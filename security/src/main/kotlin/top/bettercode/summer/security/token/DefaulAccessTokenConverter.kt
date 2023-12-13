package top.bettercode.summer.security.token

/**
 *
 * @author Peter Wu
 */
class DefaulAccessTokenConverter : AccessTokenConverter {
    override fun convert(storeToken: StoreToken): IAccessToken {
        return AccessToken(storeToken)
    }
}