package top.bettercode.summer.security.repository

import org.slf4j.LoggerFactory
import org.springframework.dao.DuplicateKeyException
import org.springframework.dao.EmptyResultDataAccessException
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.jdbc.core.RowMapper
import org.springframework.jdbc.core.support.SqlLobValue
import org.springframework.transaction.annotation.Transactional
import org.springframework.util.Assert
import top.bettercode.summer.security.token.StoreToken
import top.bettercode.summer.security.token.TokenId
import java.sql.ResultSet
import java.sql.Types
import javax.sql.DataSource


open class JdbcStoreTokenRepository @JvmOverloads constructor(dataSource: DataSource?, tableName: String = "api_token") : StoreTokenRepository {
    private val log = LoggerFactory.getLogger(JdbcStoreTokenRepository::class.java)
    private val defaultInsertStatement: String
    private val defaultSelectStatement: String
    private val defaultSelectByAccessStatement: String
    private val defaultSelectByRefreshStatement: String
    private val defaultDeleteStatement: String
    private val defaultBatchDeleteStatement: String
    private val jdkSerializationSerializer = JdkSerializationSerializer()
    private val jdbcTemplate: JdbcTemplate

    init {
        Assert.notNull(dataSource, "DataSource required")
        Assert.hasText(tableName, "tableName required")
        jdbcTemplate = JdbcTemplate(dataSource!!)
        defaultInsertStatement = ("insert into " + tableName
                + " (id, access_token, refresh_token, authentication) values (?, ?, ?, ?)")
        defaultSelectStatement = "select authentication,id from $tableName where id=?"
        defaultSelectByAccessStatement = "select authentication,id from $tableName where access_token = ?"
        defaultSelectByRefreshStatement = "select authentication,id from $tableName where refresh_token = ?"
        defaultDeleteStatement = "delete from $tableName where id=?"
        defaultBatchDeleteStatement = "delete from $tableName where id in "
    }

    @Transactional
    override fun save(storeToken: StoreToken) {
        try {
            val tokenId = storeToken.id
            remove(tokenId)
            val id = tokenId.toString()
            val accessToken = storeToken.accessToken.tokenValue
            val refreshToken = storeToken.refreshToken.tokenValue
            val auth = jdkSerializationSerializer.serialize(storeToken)
            val update = jdbcTemplate.update(defaultInsertStatement, arrayOf(id, accessToken, refreshToken, SqlLobValue(auth)), intArrayOf(Types.VARCHAR, Types.VARCHAR, Types.VARCHAR, Types.BLOB))
            if (log.isDebugEnabled) {
                log.debug("JdbcApiAuthorizationService.save\n{}\n{},{},{}\naffected:{}",
                        defaultInsertStatement, id,
                        accessToken, refreshToken, update)
            }
        } catch (e: DuplicateKeyException) {
            save(storeToken)
        }
    }

    @Transactional
    override fun remove(storeToken: StoreToken) {
        remove(storeToken.id)
    }

    @Transactional
    override fun remove(tokenId: TokenId) {
        val id = tokenId.toString()
        val update = jdbcTemplate.update(defaultDeleteStatement, id)
        if (log.isDebugEnabled) {
            log.debug("JdbcApiAuthorizationService.remove\n{}\n{}\naffected:{}", defaultDeleteStatement,
                    id, update)
        }
    }

    @Transactional
    override fun remove(tokenIds: List<TokenId>) {
        Assert.notEmpty(tokenIds, "ids must not be empty")
        val ids = tokenIds.map { it.toString() }.toTypedArray()
        val sql = defaultBatchDeleteStatement + "(${ids.joinToString(",") { "?" }})"
        @Suppress("SqlSourceToSinkFlow") val update = jdbcTemplate.update(sql, *ids)
        if (log.isDebugEnabled) {
            log.debug("JdbcApiAuthorizationService.remove\n{}\n{}\naffected:{}", sql,
                    ids, update)
        }
    }

    override fun findById(tokenId: TokenId): StoreToken? {
        return getStoreToken(tokenId.toString(), defaultSelectStatement)
    }

    override fun findByAccessToken(accessToken: String): StoreToken? {
        return getStoreToken(accessToken, defaultSelectByAccessStatement)
    }

    override fun findByRefreshToken(refreshToken: String): StoreToken? {
        return getStoreToken(refreshToken, defaultSelectByRefreshStatement)
    }

    /**
     * @param param           参数，ID/token
     * @param selectStatement 查询语句
     * @return 结果
     */
    private fun getStoreToken(param: String?, selectStatement: String): StoreToken? {
        return try {
            val storeToken = jdbcTemplate.queryForObject<StoreToken>(selectStatement,
                    RowMapper { rs: ResultSet, _: Int ->
                        val bytes = rs.getBytes(1)
                        if (JdkSerializationSerializer.isEmpty(bytes)) {
                            return@RowMapper null
                        }
                        try {
                            return@RowMapper jdkSerializationSerializer.deserialize(bytes) as StoreToken
                        } catch (e: Exception) {
                            log.warn("apiToken反序列化失败", e)
                            try {
                                val update = jdbcTemplate.update(defaultDeleteStatement, rs.getString(2))
                                if (log.isDebugEnabled) {
                                    log.debug(
                                            "JdbcApiAuthorizationService.getApiAuthenticationToken delete\n{}\n{}\naffected:{}",
                                            defaultDeleteStatement, rs.getString(2), update)
                                }
                            } catch (ex: Exception) {
                                log.warn("apiToken删除失败", ex)
                            }
                            return@RowMapper null
                        }
                    }, param)
            if (log.isDebugEnabled) {
                log.debug("JdbcApiAuthorizationService.getApiAuthenticationToken\n{}\n{}\nresult:{}",
                        selectStatement, param, storeToken?.userDetails)
            }
            storeToken
        } catch (e: EmptyResultDataAccessException) {
            null
        }
    }
}
