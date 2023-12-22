package top.bettercode.summer.tools.weixin.support

import top.bettercode.summer.security.token.AccessToken

/**
 *
 * @author Peter Wu
 */
class WeixinAccessToken : WeixinToken() {

    companion object {
        @Suppress("ConstPropertyName")
        private const val serialVersionUID = 1L

        @JvmStatic
        fun of(accessToken: AccessToken): WeixinToken {
            val token = WeixinToken()
            token.tokenType = accessToken.tokenType
            token.accessToken = accessToken.accessToken
            token.expiresIn = accessToken.expiresIn
            token.refreshToken = accessToken.refreshToken
            token.scope = accessToken.scope
            token.getAdditionalInformation().putAll(accessToken.getAdditionalInformation())
            return token
        }


    }

}