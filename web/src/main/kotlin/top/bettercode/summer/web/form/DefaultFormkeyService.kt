package top.bettercode.summer.web.form

import com.github.benmanes.caffeine.cache.Caffeine
import java.time.Duration
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ConcurrentMap
import java.util.function.Consumer

/**
 * 不支持 [FormDuplicateCheck] 自定义 ttl
 *
 * @author Peter Wu
 */
class DefaultFormkeyService(private val ttl: Duration) : FormkeyService() {
    private val caches: ConcurrentMap<Duration, ConcurrentMap<String, Boolean>> = ConcurrentHashMap()

    private fun getCache(ttl: Duration): ConcurrentMap<String, Boolean> {
        return caches.computeIfAbsent(ttl) { k: Duration ->
            val objectCache = Caffeine.newBuilder()
                    .expireAfterWrite(k).build<String, Boolean>()
            objectCache.asMap()
        }
    }

    override fun exist(formkey: String, ttl: Duration?): Boolean {
        val present = getCache(ttl ?: this.ttl).putIfAbsent(formkey, true)
        return present != null
    }

    override fun remove(formkey: String) {
        caches.values.forEach(Consumer { map: ConcurrentMap<String, Boolean> -> map.remove(formkey) })
    }
}
