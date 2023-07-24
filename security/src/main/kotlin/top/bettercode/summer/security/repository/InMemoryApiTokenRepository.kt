package top.bettercode.summer.security.repository

import top.bettercode.summer.security.token.ApiToken

/**
 * @author Peter Wu
 */
class InMemoryApiTokenRepository(
        private val tokenMap: MutableMap<String, ApiToken?>,
        private val accessTokenMap: MutableMap<String?, String>,
        private val refreshTokenMap: MutableMap<String?, String>
) : ApiTokenRepository {
    override fun save(apiToken: ApiToken) {
        val scope = apiToken.scope
        val username = apiToken.username
        val id = "$scope:$username"
        remove(scope, username)
        accessTokenMap[apiToken.accessToken.tokenValue] = id
        refreshTokenMap[apiToken.refreshToken.tokenValue] = id
        tokenMap[id] = apiToken
    }

    override fun remove(apiToken: ApiToken) {
        val scope = apiToken.scope
        val username = apiToken.username
        val id = "$scope:$username"
        accessTokenMap.remove(apiToken.accessToken.tokenValue)
        refreshTokenMap.remove(apiToken.refreshToken.tokenValue)
        tokenMap.remove(id)
    }

    override fun remove(scope: String, username: String) {
        val id = "$scope:$username"
        val authenticationToken = tokenMap[id]
        authenticationToken?.let { remove(it) }
    }

    override fun remove(scope: String, usernames: List<String>) {
        usernames.forEach { remove(scope, it) }
    }

    override fun findByScopeAndUsername(scope: String, username: String): ApiToken? {
        val id = "$scope:$username"
        return tokenMap[id]
    }

    override fun findByAccessToken(accessToken: String): ApiToken? {
        val id = accessTokenMap[accessToken]
        return if (id != null) {
            tokenMap[id]
        } else null
    }

    override fun findByRefreshToken(refreshToken: String): ApiToken? {
        val id = refreshTokenMap[refreshToken]
        return if (id != null) {
            tokenMap[id]
        } else null
    }
}
