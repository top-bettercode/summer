package top.bettercode.summer.web.form

import com.github.benmanes.caffeine.cache.Caffeine
import org.springframework.util.Assert
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ConcurrentMap
import java.util.concurrent.TimeUnit
import java.util.function.Consumer

/**
 * 不支持 [FormDuplicateCheck] 自定义 expireSeconds
 *
 * @author Peter Wu
 */
class FormkeyService(expireSeconds: Long) : IFormkeyService {
    private val expireSeconds: Long
    private val caches: ConcurrentMap<Long, ConcurrentMap<String, Boolean>>

    init {
        Assert.isTrue(expireSeconds > 0, "过期时间必须大于0")
        this.expireSeconds = expireSeconds
        caches = ConcurrentHashMap()
    }

    private fun getCache(expireSeconds: Long): ConcurrentMap<String, Boolean> {
        return caches.computeIfAbsent(expireSeconds) { k: Long? ->
            val objectCache = Caffeine.newBuilder()
                    .expireAfterWrite(k!!, TimeUnit.SECONDS).build<String, Boolean>()
            objectCache.asMap()
        }
    }

    override fun exist(formkey: String, expireSeconds: Long): Boolean {
        var seconds = expireSeconds
        seconds = if (seconds <= 0) this.expireSeconds else seconds
        val present = getCache(seconds).putIfAbsent(formkey, true)
        return present != null
    }

    override fun remove(formkey: String) {
        caches.values.forEach(Consumer { map: ConcurrentMap<String, Boolean> -> map.remove(formkey) })
    }
}
