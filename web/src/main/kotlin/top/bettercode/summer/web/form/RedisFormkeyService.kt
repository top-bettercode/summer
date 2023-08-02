package top.bettercode.summer.web.form

import org.springframework.data.redis.cache.RedisCacheWriter
import org.springframework.data.redis.connection.RedisConnectionFactory
import java.nio.charset.StandardCharsets
import java.time.Duration

/**
 * @author Peter Wu
 */
class RedisFormkeyService(connectionFactory: RedisConnectionFactory,
                          private val redisCacheName: String,
                          private val ttl: Duration) : IFormkeyService {
    private val redisCacheWriter: RedisCacheWriter

    init {
        redisCacheWriter = RedisCacheWriter.lockingRedisCacheWriter(connectionFactory)
    }

    override fun exist(formkey: String, ttl: Duration?): Boolean {
        return (redisCacheWriter.putIfAbsent(redisCacheName, formkey.toByteArray(StandardCharsets.UTF_8), byteArrayOf(1), (ttl
                ?: this.ttl))
                != null)
    }

    override fun remove(formkey: String) {
        redisCacheWriter.remove(redisCacheName, formkey.toByteArray(StandardCharsets.UTF_8))
    }
}
