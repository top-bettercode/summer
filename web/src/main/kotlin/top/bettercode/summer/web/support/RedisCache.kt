package top.bettercode.summer.web.support

import org.springframework.cache.support.NullValue
import org.springframework.core.convert.ConversionFailedException
import org.springframework.core.convert.ConversionService
import org.springframework.core.convert.TypeDescriptor
import org.springframework.data.redis.cache.CacheKeyPrefix
import org.springframework.data.redis.cache.RedisCache
import org.springframework.data.redis.cache.RedisCacheConfiguration
import org.springframework.data.redis.cache.RedisCacheWriter
import org.springframework.data.redis.connection.RedisConnectionFactory
import org.springframework.data.redis.serializer.JdkSerializationRedisSerializer
import org.springframework.data.redis.serializer.RedisSerializationContext.SerializationPair
import org.springframework.data.redis.serializer.RedisSerializer
import org.springframework.data.redis.serializer.StringRedisSerializer
import org.springframework.data.redis.util.ByteUtils
import org.springframework.format.support.DefaultFormattingConversionService
import org.springframework.util.Assert
import org.springframework.util.ObjectUtils
import org.springframework.util.ReflectionUtils
import java.nio.ByteBuffer
import java.time.Duration
import java.util.*

/**
 * @author Peter Wu
 */
class RedisCache @JvmOverloads
constructor(
        connectionFactory: RedisConnectionFactory,
        private val cacheName: String,
        expireSeconds: Long,
        private val allowNullValues: Boolean = false,
        private val usePrefix: Boolean = true,
        private val keyPrefix: CacheKeyPrefix = CacheKeyPrefix.simple(),
        private val keySerializationPair: SerializationPair<String> = SerializationPair.fromSerializer(StringRedisSerializer()),
        private val valueSerializationPair: SerializationPair<Any> = SerializationPair.fromSerializer(JdkSerializationRedisSerializer(RedisCache::class.java.classLoader))
) {
    private val binaryNullValue: ByteArray = RedisSerializer.java().serialize(NullValue.INSTANCE)!!
    private val cacheWriter: RedisCacheWriter
    private val conversionService: ConversionService
    private val expireSeconds: Duration

    init {
        cacheWriter = RedisCacheWriter.lockingRedisCacheWriter(connectionFactory)
        conversionService = DefaultFormattingConversionService()
        RedisCacheConfiguration.registerDefaultConverters(conversionService)

        this.expireSeconds = Duration.ofSeconds(expireSeconds)
    }

    @JvmOverloads
    fun put(key: String, value: Any, expireSeconds: Duration = this.expireSeconds) {
        val cacheValue = preProcessCacheValue(value)

        require(!(!allowNullValues && cacheValue == null)) {
            String.format(
                    "Cache '%s' does not allow 'null' values. Avoid storing null via '@Cacheable(unless=\"#result == null\")' or configure RedisCache to allow 'null' via RedisCacheConfiguration.",
                    cacheName)
        }

        cacheWriter.put(cacheName, createAndConvertCacheKey(key), serializeCacheValue(cacheValue!!), expireSeconds)
    }


    @Suppress("UNCHECKED_CAST")
    operator fun <T> get(key: String, type: Class<T>?): T? {
        val value: Any? = fromStoreValue(lookup(key))
        check(!(value != null && type != null && !type.isInstance(value))) { "Cached value is not of required type [" + type!!.getName() + "]: " + value }
        return value as T
    }

    fun evict(key: String) {
        cacheWriter.remove(cacheName, createAndConvertCacheKey(key))
    }

    fun clear() {
        val pattern = conversionService.convert(createCacheKey("*"), ByteArray::class.java)!!
        cacheWriter.clean(cacheName, pattern)
    }

    private fun lookup(key: Any): Any? {
        val value = cacheWriter[cacheName, createAndConvertCacheKey(key)] ?: return null
        return deserializeCacheValue(value)
    }


    /**
     * Convert the given value from the internal store to a user value
     * returned from the get method (adapting `null`).
     * @param storeValue the store value
     * @return the value to return to the user
     */

    private fun fromStoreValue(storeValue: Any?): Any? {
        return if (allowNullValues && storeValue === NullValue.INSTANCE) {
            null
        } else storeValue
    }


    /**
     * Customization hook called before passing object to
     * [org.springframework.data.redis.serializer.RedisSerializer].
     *
     * @param value can be null.
     * @return preprocessed value. Can be null.
     */

    private fun preProcessCacheValue(value: Any?): Any? {
        if (value != null) {
            return value
        }
        return if (allowNullValues) NullValue.INSTANCE else null
    }

    private fun createAndConvertCacheKey(key: Any): ByteArray {
        return serializeCacheKey(createCacheKey(key))
    }


    /**
     * Customization hook for creating cache key before it gets serialized.
     *
     * @param key will never be null.
     * @return never null.
     */
    private fun createCacheKey(key: Any?): String {
        val convertedKey = convertKey(key!!)
        return if (!usePrefix) {
            convertedKey
        } else prefixCacheKey(convertedKey)
    }

    private fun prefixCacheKey(key: String): String {
        // allow contextual cache names by computing the key prefix on every call.
        return getKeyPrefixFor(cacheName) + key
    }

    /**
     * Get the computed key prefix for a given cacheName.
     *
     * @return never null.
     * @since 2.0.4
     */
    private fun getKeyPrefixFor(cacheName: String): String {
        Assert.notNull(cacheName, "Cache name must not be null!")
        return keyPrefix.compute(cacheName)
    }


    /**
     * Convert `key` to a [String] representation used for cache key creation.
     *
     * @param key will never be null.
     * @return never null.
     * @throws IllegalStateException if `key` cannot be converted to [String].
     */
    private fun convertKey(key: Any): String {
        if (key is String) {
            return key
        }
        val source = TypeDescriptor.forObject(key)!!
        if (conversionService.canConvert(source, TypeDescriptor.valueOf(String::class.java))) {
            return try {
                conversionService.convert(key, String::class.java)!!
            } catch (e: ConversionFailedException) {

                // may fail if the given key is a collection
                if (isCollectionLikeOrMap(source)) {
                    return convertCollectionLikeOrMapKey(key, source)
                }
                throw e
            }
        }
        val toString = ReflectionUtils.findMethod(key.javaClass, "toString")
        if (toString != null && Any::class.java != toString.declaringClass) {
            return key.toString()
        }
        throw IllegalStateException(String.format(
                "Cannot convert cache key %s to String. Please register a suitable Converter via 'RedisCacheConfiguration.configureKeyConverters(...)' or override '%s.toString()'.",
                source, key.javaClass.getSimpleName()))
    }

    /**
     * Serialize the key.
     *
     * @param cacheKey must not be null.
     * @return never null.
     */
    private fun serializeCacheKey(cacheKey: String): ByteArray {
        return ByteUtils.getBytes(keySerializationPair.write(cacheKey))
    }

    /**
     * Serialize the value to cache.
     *
     * @param value must not be null.
     * @return never null.
     */
    private fun serializeCacheValue(value: Any): ByteArray {
        return if (allowNullValues && value is NullValue) {
            binaryNullValue
        } else ByteUtils.getBytes(valueSerializationPair.write(value))
    }

    /**
     * Deserialize the given value to the actual cache value.
     *
     * @param value must not be null.
     * @return can be null.
     */

    private fun deserializeCacheValue(value: ByteArray): Any {
        return if (allowNullValues && ObjectUtils.nullSafeEquals(value, binaryNullValue)) {
            NullValue.INSTANCE
        } else valueSerializationPair.read(ByteBuffer.wrap(value))
    }

    private fun convertCollectionLikeOrMapKey(key: Any, source: TypeDescriptor): String {
        if (source.isMap) {
            val target = StringBuilder("{")
            for ((key1, value) in key as Map<*, *>) {
                target.append(convertKey(key1!!)).append("=").append(convertKey(value!!))
            }
            target.append("}")
            return target.toString()
        } else if (source.isCollection || source.isArray) {
            val sj = StringJoiner(",")
            val collection = if (source.isCollection) key as Collection<*> else listOf(*ObjectUtils.toObjectArray(key))
            for (`val` in collection) {
                sj.add(convertKey(`val`!!))
            }
            return "[$sj]"
        }
        throw IllegalArgumentException(String.format("Cannot convert cache key %s to String.", key))
    }

    private fun isCollectionLikeOrMap(source: TypeDescriptor): Boolean {
        return source.isArray || source.isCollection || source.isMap
    }

}
