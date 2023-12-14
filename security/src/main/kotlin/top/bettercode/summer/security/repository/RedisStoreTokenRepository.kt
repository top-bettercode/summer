package top.bettercode.summer.security.repository

import org.slf4j.LoggerFactory
import org.springframework.data.redis.connection.RedisConnection
import org.springframework.data.redis.connection.RedisConnectionFactory
import org.springframework.data.redis.connection.RedisPipelineException
import org.springframework.util.ClassUtils
import org.springframework.util.ReflectionUtils
import top.bettercode.summer.security.token.StoreToken
import top.bettercode.summer.security.token.TokenId
import java.lang.reflect.Method
import java.nio.charset.StandardCharsets
import kotlin.math.max

class RedisStoreTokenRepository @JvmOverloads constructor(private val connectionFactory: RedisConnectionFactory, prefix: String = "") : StoreTokenRepository {
    private val log = LoggerFactory.getLogger(RedisStoreTokenRepository::class.java)
    private var keyPrefix: String? = null
    private val jdkSerializationSerializer = JdkSerializationSerializer()
    private var redisconnectionset20: Method? = null

    init {
        keyPrefix = if (prefix.isNotBlank()) {
            "$API_AUTH$prefix:"
        } else {
            API_AUTH
        }
        if (springdataredis20) {
            loadredisconnectionmethods20()
        }
    }

    private fun loadredisconnectionmethods20() {
        redisconnectionset20 = ReflectionUtils.findMethod(
                RedisConnection::class.java, "set", ByteArray::class.java, ByteArray::class.java)
    }

    private val connection: RedisConnection
        get() = connectionFactory.connection

    private fun <T> RedisConnection.use(run: (RedisConnection) -> T?): T? {
        @Suppress("ConvertTryFinallyToUseCall")
        try {
            return run(this)
        } finally {
            this.close()
        }
    }

    private fun serializeKey(`object`: String): ByteArray {
        return (keyPrefix + `object`).toByteArray(StandardCharsets.UTF_8)
    }

    override fun save(storeToken: StoreToken) {
        try {
            val id = storeToken.id.toString()
            val auth = jdkSerializationSerializer.serialize(storeToken)
            val accessKey = serializeKey(ACCESS_TOKEN + storeToken.accessToken.tokenValue)
            val refreshKey = serializeKey(
                    REFRESH_TOKEN + storeToken.refreshToken.tokenValue)
            val idKey = serializeKey(ID + id)
            connection.use { conn ->
                val exist = getStoreToken(idKey, conn)
                conn.openPipeline()
                //删除已存在
                if (exist != null) {
                    val existAccessKey = serializeKey(
                            ACCESS_TOKEN + exist.accessToken.tokenValue)
                    val existRefreshKey = serializeKey(
                            REFRESH_TOKEN + exist.refreshToken.tokenValue)
                    if (!existAccessKey.contentEquals(accessKey)) {
                        conn.keyCommands().del(existAccessKey)
                    }
                    if (!existRefreshKey.contentEquals(refreshKey)) {
                        conn.keyCommands().del(existRefreshKey)
                    }
                }
                if (springdataredis20) {
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
                val accessExpiresIn = storeToken.accessToken.expires_in
                val refreshExpiresIn = storeToken.refreshToken.expires_in
                conn.keyCommands().expire(accessKey, accessExpiresIn.toLong())
                conn.keyCommands().expire(refreshKey, refreshExpiresIn.toLong())
                conn.keyCommands().expire(idKey, max(accessExpiresIn, refreshExpiresIn).toLong())
                conn.closePipeline()
            }
        } catch (e: Exception) {
            throw RuntimeException("保存授权信息失败", e)
        }
    }

    override fun remove(storeToken: StoreToken) {
        try {
            val id = storeToken.toString()
            val accessKey = serializeKey(ACCESS_TOKEN + storeToken.accessToken.tokenValue)
            val refreshKey = serializeKey(
                    REFRESH_TOKEN + storeToken.refreshToken.tokenValue)
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

    override fun remove(tokenId: TokenId) {
        try {
            val id = tokenId.toString()
            val idKey = serializeKey(ID + id)
            connection.use { conn ->
                val storeToken = getStoreToken(idKey, conn)
                if (storeToken != null) {
                    conn.openPipeline()
                    val accessKey = serializeKey(
                            ACCESS_TOKEN + storeToken.accessToken.tokenValue)
                    val refreshKey = serializeKey(
                            REFRESH_TOKEN + storeToken.refreshToken.tokenValue)
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

    override fun remove(tokenIds: List<TokenId>) {
        tokenIds.forEach { remove(it) }
    }

    override fun findById(tokenId: TokenId): StoreToken? {
        val id = tokenId.toString()
        val idKey = serializeKey(ID + id)
        return findByIdKey(idKey)
    }

    private fun findByIdKey(idKey: ByteArray): StoreToken? {
        return connection.use { conn -> return@use getStoreToken(idKey, conn) }
    }

    private fun getStoreToken(idKey: ByteArray, conn: RedisConnection): StoreToken? {
        return try {
            val bytes = conn.stringCommands()[idKey]
            if (JdkSerializationSerializer.isEmpty(bytes)) {
                return null
            }
            try {
                jdkSerializationSerializer.deserialize(bytes!!) as StoreToken
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

    override fun findByAccessToken(accessToken: String): StoreToken? {
        try {
            val accessKey = serializeKey(ACCESS_TOKEN + accessToken)
            return connection.use { conn ->
                val bytes = conn.stringCommands()[accessKey]
                return@use if (JdkSerializationSerializer.isEmpty(bytes)) {
                    null
                } else getStoreToken(bytes!!, conn)
            }
        } catch (e: Exception) {
            throw RuntimeException("查询授权信息失败", e)
        }
    }

    override fun findByRefreshToken(refreshToken: String): StoreToken? {
        try {
            val refreshKey = serializeKey(REFRESH_TOKEN + refreshToken)
            return connection.use { conn ->
                val bytes = conn.stringCommands()[refreshKey]
                return@use if (JdkSerializationSerializer.isEmpty(bytes)) {
                    null
                } else getStoreToken(bytes!!, conn)
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
        private val springdataredis20 = ClassUtils.isPresent(
                "org.springframework.data.redis.connection.RedisStandaloneConfiguration",
                RedisStoreTokenRepository::class.java.classLoader)
    }
}
