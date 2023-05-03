package top.bettercode.summer.security.repository

import org.slf4j.LoggerFactory
import org.springframework.data.redis.connection.RedisConnection
import org.springframework.data.redis.connection.RedisConnectionFactory
import org.springframework.data.redis.connection.RedisPipelineException
import org.springframework.util.ClassUtils
import org.springframework.util.ReflectionUtils
import org.springframework.util.StringUtils
import top.bettercode.summer.security.token.ApiToken
import java.lang.reflect.Method
import java.nio.charset.StandardCharsets
import java.util.*

class RedisApiTokenRepository @JvmOverloads constructor(private val connectionFactory: RedisConnectionFactory, prefix: String = "") : ApiTokenRepository {
    private val log = LoggerFactory.getLogger(RedisApiTokenRepository::class.java)
    private var keyPrefix: String? = null
    private val jdkSerializationSerializer = JdkSerializationSerializer()
    private var redisconnectionset20: Method? = null

    init {
        keyPrefix = if (StringUtils.hasText(prefix)) {
            "$API_AUTH$prefix:"
        } else {
            API_AUTH
        }
        if (springDataRedis_2_0) {
            loadRedisConnectionMethods_2_0()
        }
    }

    private fun loadRedisConnectionMethods_2_0() {
        redisconnectionset20 = ReflectionUtils.findMethod(
                RedisConnection::class.java, "set", ByteArray::class.java, ByteArray::class.java)
    }

    private val connection: RedisConnection
        get() = connectionFactory.connection

    private fun serializeKey(`object`: String): ByteArray {
        return (keyPrefix + `object`).toByteArray(StandardCharsets.UTF_8)
    }

    override fun save(apiToken: ApiToken) {
        try {
            val scope = apiToken.scope
            val username = apiToken.username
            val id = "$scope:$username"
            val auth = jdkSerializationSerializer.serialize(apiToken)
            val accessKey = serializeKey(ACCESS_TOKEN + apiToken.accessToken.tokenValue)
            val refreshKey = serializeKey(
                    REFRESH_TOKEN + apiToken.refreshToken.tokenValue)
            val idKey = serializeKey(ID + id)
            connection.use { conn ->
                val exist = getApiToken(idKey, conn)
                conn.openPipeline()
                //删除已存在
                if (exist != null) {
                    val existAccessKey = serializeKey(
                            ACCESS_TOKEN + exist.accessToken.tokenValue)
                    val existRefreshKey = serializeKey(
                            REFRESH_TOKEN + exist.refreshToken.tokenValue)
                    if (!Arrays.equals(existAccessKey, accessKey)) {
                        conn.keyCommands().del(existAccessKey)
                    }
                    if (!Arrays.equals(existRefreshKey, refreshKey)) {
                        conn.keyCommands().del(existRefreshKey)
                    }
                }
                if (springDataRedis_2_0) {
                    try {
                        redisconnectionset20!!.invoke(conn, accessKey, idKey)
                        redisconnectionset20!!.invoke(conn, refreshKey, idKey)
                        redisconnectionset20!!.invoke(conn, idKey, auth)
                    } catch (ex: Exception) {
                        conn.closePipeline()
                        throw RuntimeException(ex)
                    }
                } else {
                    conn.stringCommands()[accessKey] = idKey
                    conn.stringCommands()[refreshKey] = idKey
                    conn.stringCommands()[idKey] = auth
                }
                val access_expires_in = apiToken.accessToken.expires_in
                val refresh_expires_in = apiToken.refreshToken.expires_in
                conn.keyCommands().expire(accessKey, access_expires_in.toLong())
                conn.keyCommands().expire(refreshKey, refresh_expires_in.toLong())
                conn.keyCommands().expire(idKey, Math.max(access_expires_in, refresh_expires_in).toLong())
                conn.closePipeline()
            }
        } catch (e: Exception) {
            throw RuntimeException("保存授权信息失败", e)
        }
    }

    override fun remove(apiToken: ApiToken) {
        try {
            val scope = apiToken.scope
            val username = apiToken.username
            val id = "$scope:$username"
            val accessKey = serializeKey(ACCESS_TOKEN + apiToken.accessToken.tokenValue)
            val refreshKey = serializeKey(
                    REFRESH_TOKEN + apiToken.refreshToken.tokenValue)
            val idKey = serializeKey(ID + id)
            connection.use { conn ->
                conn.openPipeline()
                conn.keyCommands().del(accessKey)
                conn.keyCommands().del(refreshKey)
                conn.keyCommands().del(idKey)
                conn.closePipeline()
            }
        } catch (e: Exception) {
            throw RuntimeException("移除授权信息失败", e)
        }
    }

    override fun remove(scope: String?, username: String?) {
        try {
            val id = "$scope:$username"
            val idKey = serializeKey(ID + id)
            connection.use { conn ->
                val apiAuthenticationToken = getApiToken(idKey, conn)
                if (apiAuthenticationToken != null) {
                    conn.openPipeline()
                    val accessKey = serializeKey(
                            ACCESS_TOKEN + apiAuthenticationToken.accessToken.tokenValue)
                    val refreshKey = serializeKey(
                            REFRESH_TOKEN + apiAuthenticationToken.refreshToken.tokenValue)
                    conn.keyCommands().del(accessKey)
                    conn.keyCommands().del(refreshKey)
                    conn.keyCommands().del(idKey)
                    conn.closePipeline()
                }
            }
        } catch (e: RedisPipelineException) {
            throw RuntimeException("移除授权信息失败", e)
        }
    }

    override fun findByScopeAndUsername(scope: String, username: String): ApiToken? {
        val id = "$scope:$username"
        val idKey = serializeKey(ID + id)
        return findByIdKey(idKey)
    }

    private fun findByIdKey(idKey: ByteArray): ApiToken? {
        connection.use { conn -> return getApiToken(idKey, conn) }
    }

    private fun getApiToken(idKey: ByteArray, conn: RedisConnection): ApiToken? {
        return try {
            val bytes = conn.stringCommands()[idKey]
            if (JdkSerializationSerializer.isEmpty(bytes)) {
                return null
            }
            try {
                jdkSerializationSerializer.deserialize(bytes!!) as ApiToken
            } catch (e: Exception) {
                log.warn("apiToken反序列化失败", e)
                try {
                    conn.keyCommands().del(idKey)
                } catch (ex: Exception) {
                    log.warn("apiToken删除失败", ex)
                }
                null
            }
        } catch (e: Exception) {
            throw RuntimeException("查询授权信息失败", e)
        }
    }

    override fun findByAccessToken(accessToken: String?): ApiToken? {
        try {
            val accessKey = serializeKey(ACCESS_TOKEN + accessToken)
            connection.use { conn ->
                val bytes = conn.stringCommands()[accessKey]
                return if (JdkSerializationSerializer.isEmpty(bytes)) {
                    null
                } else getApiToken(bytes!!, conn)
            }
        } catch (e: Exception) {
            throw RuntimeException("查询授权信息失败", e)
        }
    }

    override fun findByRefreshToken(refreshToken: String): ApiToken? {
        try {
            val refreshKey = serializeKey(REFRESH_TOKEN + refreshToken)
            connection.use { conn ->
                val bytes = conn.stringCommands()[refreshKey]
                return if (JdkSerializationSerializer.isEmpty(bytes)) {
                    null
                } else getApiToken(bytes!!, conn)
            }
        } catch (e: Exception) {
            throw RuntimeException("查询授权信息失败", e)
        }
    }

    companion object {
        private const val API_AUTH = "api_auth:"
        private const val ID = "id:"
        private const val ACCESS_TOKEN = "access_token:"
        private const val REFRESH_TOKEN = "refresh_token:"
        private val springDataRedis_2_0 = ClassUtils.isPresent(
                "org.springframework.data.redis.connection.RedisStandaloneConfiguration",
                RedisApiTokenRepository::class.java.classLoader)
    }
}
