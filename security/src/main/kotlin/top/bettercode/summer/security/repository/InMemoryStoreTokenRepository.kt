package top.bettercode.summer.security.repository

import org.slf4j.LoggerFactory
import top.bettercode.summer.security.token.StoreToken
import top.bettercode.summer.security.token.TokenId

/**
 * @author Peter Wu
 */
class InMemoryStoreTokenRepository(
    private val tokenMap: MutableMap<String, StoreToken?>,
    private val accessTokenMap: MutableMap<String?, String>,
    private val refreshTokenMap: MutableMap<String?, String>
) : StoreTokenRepository {

    private val log = LoggerFactory.getLogger(InMemoryStoreTokenRepository::class.java)

    override fun save(storeToken: StoreToken) {
        val tokenId = storeToken.id
        val id = tokenId.toString()
        log.info("save : $id")
        remove(tokenId)
        accessTokenMap[storeToken.accessToken.tokenValue] = id
        refreshTokenMap[storeToken.refreshToken.tokenValue] = id
        tokenMap[id] = storeToken
    }

    override fun remove(storeToken: StoreToken) {
        val tokenId = storeToken.id
        val id = tokenId.toString()
        log.info("remove : $id")
        accessTokenMap.remove(storeToken.accessToken.tokenValue)
        refreshTokenMap.remove(storeToken.refreshToken.tokenValue)
        tokenMap.remove(id)
    }

    override fun remove(tokenId: TokenId) {
        val storeToken = tokenMap[tokenId.toString()]
        storeToken?.let { remove(it) }
    }

    override fun remove(tokenIds: List<TokenId>) {
        tokenIds.forEach { remove(it) }
    }

    override fun findById(tokenId: TokenId): StoreToken? {
        val id = tokenId.toString()
        log.info("findById : $id")
        return tokenMap[id]
    }

    override fun findByAccessToken(accessToken: String): StoreToken? {
        log.info("findByAccessToken : $accessToken")
        val id = accessTokenMap[accessToken]
        return if (id != null) {
            tokenMap[id]
        } else null
    }

    override fun findByRefreshToken(refreshToken: String): StoreToken? {
        log.info("findByRefreshToken : $refreshToken")
        val id = refreshTokenMap[refreshToken]
        return if (id != null) {
            tokenMap[id]
        } else null
    }
}
