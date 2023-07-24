package top.bettercode.summer.security.repository

import org.springframework.lang.Nullable
import top.bettercode.summer.security.token.ApiToken

interface ApiTokenRepository {
    fun save(apiToken: ApiToken)
    fun remove(apiToken: ApiToken)
    fun remove(scope: String, username: String)
    fun remove(scope: String, usernames: List<String>)
    @Nullable
    fun findByScopeAndUsername(scope: String, username: String): ApiToken?

    @Nullable
    fun findByAccessToken(accessToken: String): ApiToken?

    @Nullable
    fun findByRefreshToken(refreshToken: String): ApiToken?
}
