package top.bettercode.summer.tools.weixin.properties

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.util.StringUtils
import top.bettercode.summer.tools.weixin.support.WeixinToken
import java.io.UnsupportedEncodingException
import java.net.URLEncoder

/**
 * @author Peter Wu
 */
interface IOffiaccountProperties : IWexinProperties {
    val useUnionid: Boolean
        get() = false
    val appBaseUrl: String?
    val wechatBaseUrl: String?
    val wechatWebOauthUrl: String
    val oauthUrl: String?
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
                + IWexinProperties.OPEN_ID_NAME + "=" + openId + "&hasBound=" + StringUtils.hasText(token) + "&forceLogin="
                + forceLogin + "&state=" + (state ?: "") + "&msg=" + encodeMsg + "&_timer="
                + System.currentTimeMillis())
    }

    companion object {
        val log: Logger = LoggerFactory.getLogger(IOffiaccountProperties::class.java)
        const val OAUTH_URL = "/wechat/oauth"
    }
}
