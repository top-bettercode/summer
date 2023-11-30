package top.bettercode.summer.tools.weixin.support

import org.springframework.data.redis.connection.RedisConnectionFactory
import top.bettercode.summer.tools.weixin.support.offiaccount.entity.CachedValue
import top.bettercode.summer.web.support.RedisCache
import java.time.Duration

/**
 *
 * @author Peter Wu
 */
class RedisWeixinCache(cacheSeconds: Long,
                       cacheName: String,
                       redisConnectionFactory: RedisConnectionFactory) : IWeixinCache {

    private val cache: RedisCache = RedisCache(redisConnectionFactory, cacheName,
            Duration.ofSeconds(cacheSeconds))

    override fun put(key: String, value: CachedValue) {
        cache.put(key, value, value.expiresIn)
    }

    override fun get(key: String): CachedValue? {
        return cache[key, CachedValue::class.java]
    }

    override fun evict(key: String) {
        cache.evict(key)
    }

    override fun clear() {
        cache.clear()
    }
}