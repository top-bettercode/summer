package top.bettercode.summer.security.repository

import org.springframework.core.convert.converter.Converter
import org.springframework.core.serializer.support.DeserializingConverter
import org.springframework.core.serializer.support.SerializingConverter
import org.springframework.lang.Nullable
import org.springframework.util.Assert

class JdkSerializationSerializer @JvmOverloads constructor(
        serializer: Converter<Any, ByteArray> = SerializingConverter(),
        deserializer: Converter<ByteArray, Any> = DeserializingConverter()
) {
    private val serializer: Converter<Any, ByteArray>
    private val deserializer: Converter<ByteArray, Any>

    constructor(@Nullable classLoader: ClassLoader?) : this(SerializingConverter(), DeserializingConverter(classLoader!!))

    init {
        Assert.notNull(serializer, "Serializer must not be null!")
        Assert.notNull(deserializer, "Deserializer must not be null!")
        this.serializer = serializer
        this.deserializer = deserializer
    }

    fun deserialize(@Nullable bytes: ByteArray): Any? {
        return if (isEmpty(bytes)) {
            null
        } else try {
            deserializer.convert(bytes)
        } catch (ex: Exception) {
            throw RuntimeException("Cannot deserialize", ex)
        }
    }

    fun serialize(@Nullable `object`: Any?): ByteArray {
        return if (`object` == null) {
            EMPTY_ARRAY
        } else try {
            serializer.convert(`object`)
        } catch (ex: Exception) {
            throw RuntimeException("Cannot serialize", ex)
        }
    }

    companion object {
        val EMPTY_ARRAY = ByteArray(0)
        fun isEmpty(bytes: ByteArray?): Boolean {
            return bytes == null || bytes.isEmpty()
        }
    }
}
