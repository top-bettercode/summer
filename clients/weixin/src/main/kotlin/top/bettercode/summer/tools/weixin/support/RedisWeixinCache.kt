package top.bettercode.summer.tools.weixin.support

import org.springframework.data.redis.connection.RedisConnectionFactory
import top.bettercode.summer.tools.lang.ExpiringValue
import top.bettercode.summer.web.support.RedisCache
import java.time.Duration

/**
 *
 * @author Peter Wu
 */
class RedisWeixinCache(
    cacheSeconds: Long,
    cacheName: String,
    redisConnectionFactory: RedisConnectionFactory
) : IWeixinCache {

    private val log = org.slf4j.LoggerFactory.getLogger(this.javaClass)
    private val cache: RedisCache = RedisCache(
        redisConnectionFactory, cacheName,
        Duration.ofSeconds(cacheSeconds)
    )

    override fun put(key: String, value: ExpiringValue<String>) {
        cache.put(key, value, value.expiresIn)
    }

    override fun get(key: String): ExpiringValue<String>? {
        try {
            @Suppress("UNCHECKED_CAST")
            return cache[key, ExpiringValue::class.java] as ExpiringValue<String>?
        } catch (e: Exception) {
            log.error(e.message, e)
            return null
        }
    }

    override fun evict(key: String) {
        cache.evict(key)
    }

    override fun clear() {
        cache.clear()
    }
}