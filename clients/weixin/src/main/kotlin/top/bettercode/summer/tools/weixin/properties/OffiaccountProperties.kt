package top.bettercode.summer.tools.weixin.properties

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.boot.context.properties.ConfigurationProperties
import top.bettercode.summer.tools.weixin.support.WeixinToken
import java.io.UnsupportedEncodingException
import java.net.URLEncoder

/**
 * @author Peter Wu
 */
@ConfigurationProperties(prefix = "summer.wechat")
open class OffiaccountProperties : WeixinProperties("微信公众号") {
    //--------------------------------------------
    var useUnionid = false

    /**
     * 项目部署的URL地址
     */
    var appBaseUrl: String? = null

    /**
     * 微信前端基础地址
     */
    var wechatBaseUrl: String? = null

    /**
     * 微信前端授权页面地址
     */
    var wechatWebOauthUrl = ""

    val oauthUrl: String
        get() = (appBaseUrl ?: "") + OAUTH_URL

    fun redirectUrl(weixinToken: WeixinToken?, forceLogin: Boolean, state: String?): String {
        return "redirect:" + wechatUrl(weixinToken, forceLogin, state)
    }

    fun wechatUrl(weixinToken: WeixinToken?, forceLogin: Boolean, state: String?): String {
        val token = weixinToken?.accessToken ?: ""
        val openId = weixinToken?.openId ?: ""
        val msg = weixinToken?.message ?: ""
        val encodeMsg: String = try {
            URLEncoder.encode(msg, "UTF-8")
        } catch (e: UnsupportedEncodingException) {
            log.warn(e.message, e)
            ""
        }
        return (wechatBaseUrl + wechatWebOauthUrl + "?access_token=" + token + "&"
                + OPEN_ID_NAME + "=" + openId + "&hasBound=" + token.isNotBlank() + "&forceLogin="
                + forceLogin + "&state=" + (state ?: "") + "&msg=" + encodeMsg + "&_timer="
                + System.currentTimeMillis())
    }

    companion object {
        val log: Logger = LoggerFactory.getLogger(OffiaccountProperties::class.java)
        const val OAUTH_URL = "/wechat/oauth"
    }

}
