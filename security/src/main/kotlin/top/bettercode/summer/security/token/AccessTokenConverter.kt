package top.bettercode.summer.security.token

/**
 *
 * @author Peter Wu
 */
interface AccessTokenConverter {

    fun convert(storeToken: StoreToken): IAccessToken
}