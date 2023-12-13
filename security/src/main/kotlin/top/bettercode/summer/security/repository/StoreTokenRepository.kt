package top.bettercode.summer.security.repository

import top.bettercode.summer.security.token.StoreToken
import top.bettercode.summer.security.token.TokenId

interface StoreTokenRepository {
    fun save(storeToken: StoreToken)
    fun remove(storeToken: StoreToken)
    fun remove(tokenId: TokenId)
    fun remove(tokenIds: List<TokenId>)
    fun findById(tokenId: TokenId): StoreToken?
    fun findByAccessToken(accessToken: String): StoreToken?
    fun findByRefreshToken(refreshToken: String): StoreToken?
}
