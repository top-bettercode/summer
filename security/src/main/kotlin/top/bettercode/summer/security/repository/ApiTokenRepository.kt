package top.bettercode.summer.security.repository

import top.bettercode.summer.security.token.ApiToken

interface ApiTokenRepository {
    fun save(apiToken: ApiToken)
    fun remove(apiToken: ApiToken)
    fun remove(scope: String, username: String)
    fun remove(scope: String, usernames: List<String>)

    fun findByScopeAndUsername(scope: String, username: String): ApiToken?

    fun findByAccessToken(accessToken: String): ApiToken?

    fun findByRefreshToken(refreshToken: String): ApiToken?
}
