package top.bettercode.summer.tools.weixin.support

import com.github.benmanes.caffeine.cache.Cache
import com.github.benmanes.caffeine.cache.Caffeine
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import top.bettercode.summer.tools.weixin.support.offiaccount.entity.CachedValue
import java.util.concurrent.TimeUnit

/**
 *
 * @author Peter Wu
 */
class DefaultWeixinCache(cacheSeconds: Long) : IWeixinCache {

    private val log: Logger = LoggerFactory.getLogger(DefaultWeixinCache::class.java)
    private val cache: Cache<String, CachedValue> = Caffeine.newBuilder().expireAfterWrite(cacheSeconds, TimeUnit.SECONDS)
            .maximumSize(1000).build()

    override fun put(key: String, value: CachedValue) {
        cache.put(key, value)
    }

    override fun get(key: String): CachedValue? {
        try {
            return cache.getIfPresent(key)
        } catch (e: Exception) {
            log.error(e.message, e)
            return null
        }
    }

    override fun evict(key: String) {
        cache.invalidate(key)
    }

    override fun clear() {
        cache.invalidateAll()
    }
}