package top.bettercode.summer.security.repository

import org.slf4j.LoggerFactory
import org.springframework.dao.DuplicateKeyException
import org.springframework.dao.EmptyResultDataAccessException
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.jdbc.core.support.SqlLobValue
import org.springframework.transaction.annotation.Transactional
import org.springframework.util.Assert
import top.bettercode.summer.security.repository.JdbcApiTokenRepository
import top.bettercode.summer.security.token.ApiToken
import top.bettercode.summer.tools.lang.util.ArrayUtil.isEmpty
import java.sql.ResultSet
import java.sql.Types
import javax.sql.DataSource

open class JdbcApiTokenRepository @JvmOverloads constructor(dataSource: DataSource?, tableName: String = "api_token") : ApiTokenRepository {
    private val log = LoggerFactory.getLogger(JdbcApiTokenRepository::class.java)
    private val defaultInsertStatement: String
    private val defaultSelectStatement: String
    private val defaultSelectByAccessStatement: String
    private val defaultSelectByRefreshStatement: String
    private val defaultDeleteStatement: String
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
    }

    @Transactional
    override fun save(apiToken: ApiToken) {
        try {
            val scope = apiToken.scope
            val username = apiToken.username
            val id = "$scope:$username"
            remove(scope, username)
            val accessToken = apiToken.accessToken.tokenValue
            val refreshToken = apiToken.refreshToken.tokenValue
            val auth = jdkSerializationSerializer.serialize(apiToken)
            val update = jdbcTemplate.update(defaultInsertStatement, arrayOf(id, accessToken, refreshToken, SqlLobValue(auth)), intArrayOf(Types.VARCHAR, Types.VARCHAR, Types.VARCHAR, Types.BLOB))
            if (log.isDebugEnabled) {
                log.debug("JdbcApiAuthorizationService.save\n{}\n{},{},{}\naffected:{}",
                        defaultInsertStatement, id,
                        accessToken, refreshToken, update)
            }
        } catch (e: DuplicateKeyException) {
            save(apiToken)
        }
    }

    @Transactional
    override fun remove(apiToken: ApiToken) {
        val scope = apiToken.scope
        val username = apiToken.username
        remove(scope, username)
    }

    @Transactional
    override fun remove(scope: String?, username: String?) {
        val id = "$scope:$username"
        val update = jdbcTemplate.update(defaultDeleteStatement, id)
        if (log.isDebugEnabled) {
            log.debug("JdbcApiAuthorizationService.remove\n{}\n{}\naffected:{}", defaultDeleteStatement,
                    id, update)
        }
    }

    override fun findByScopeAndUsername(scope: String, username: String): ApiToken? {
        val id = "$scope:$username"
        return getApiToken(id, defaultSelectStatement)
    }

    override fun findByAccessToken(accessToken: String?): ApiToken? {
        return getApiToken(accessToken, defaultSelectByAccessStatement)
    }

    override fun findByRefreshToken(refreshToken: String): ApiToken? {
        return getApiToken(refreshToken, defaultSelectByRefreshStatement)
    }

    /**
     * @param param           参数，ID/token
     * @param selectStatement 查询语句
     * @return 结果
     */
    private fun getApiToken(param: String?, selectStatement: String): ApiToken? {
        return try {
            val apiToken = jdbcTemplate.queryForObject<ApiToken>(selectStatement,
                    { rs: ResultSet, _: Int ->
                        val bytes = rs.getBytes(1)
                        if (JdkSerializationSerializer.Companion.isEmpty(bytes)) {
                            return@queryForObject null
                        }
                        try {
                            return@queryForObject jdkSerializationSerializer.deserialize(bytes) as ApiToken
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
                            return@queryForObject null
                        }
                    }, param)
            if (log.isDebugEnabled) {
                log.debug("JdbcApiAuthorizationService.getApiAuthenticationToken\n{}\n{}\nresult:{}",
                        selectStatement, param, apiToken?.userDetails)
            }
            apiToken
        } catch (e: EmptyResultDataAccessException) {
            null
        }
    }
}
