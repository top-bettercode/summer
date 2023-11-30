package top.bettercode.summer.tools.weixin.support

import com.github.benmanes.caffeine.cache.Cache
import com.github.benmanes.caffeine.cache.Caffeine
import top.bettercode.summer.tools.weixin.support.offiaccount.entity.CachedValue
import java.util.concurrent.TimeUnit

/**
 *
 * @author Peter Wu
 */
class DefaultWeixinCache(cacheSeconds: Long) : IWeixinCache {

    private val cache: Cache<String, CachedValue> = Caffeine.newBuilder().expireAfterWrite(cacheSeconds, TimeUnit.SECONDS)
            .maximumSize(1000).build()

    override fun put(key: String, value: CachedValue) {
        cache.put(key, value)
    }

    override fun get(key: String): CachedValue? {
        return cache.getIfPresent(key)
    }

    override fun evict(key: String) {
        cache.invalidate(key)
    }

    override fun clear() {
        cache.invalidateAll()
    }
}