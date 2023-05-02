package top.bettercode.summer.web.form

import org.springframework.data.redis.cache.RedisCacheWriter
import org.springframework.data.redis.connection.RedisConnectionFactory
import java.nio.charset.StandardCharsets
import java.time.Duration

/**
 * @author Peter Wu
 */
class RedisFormkeyService(connectionFactory: RedisConnectionFactory, private val redisCacheName: String,
                          expireSeconds: Long) : IFormkeyService {
    private val redisCacheWriter: RedisCacheWriter
    private val expireSeconds: Duration

    init {
        redisCacheWriter = RedisCacheWriter.lockingRedisCacheWriter(connectionFactory)
        this.expireSeconds = Duration.ofSeconds(expireSeconds)
    }

    override fun exist(formkey: String, expireSeconds: Long): Boolean {
        return (redisCacheWriter.putIfAbsent(redisCacheName, formkey.toByteArray(StandardCharsets.UTF_8), byteArrayOf(1), if (expireSeconds <= 0) this.expireSeconds else Duration.ofSeconds(expireSeconds))
                != null)
    }

    override fun remove(formkey: String) {
        redisCacheWriter.remove(redisCacheName, formkey.toByteArray(StandardCharsets.UTF_8))
    }
}
