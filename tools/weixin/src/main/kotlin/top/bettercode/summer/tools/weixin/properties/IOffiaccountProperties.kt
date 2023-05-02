package top.bettercode.summer.tools.weixin.properties

import org.slf4j.LoggerFactory
import org.springframework.util.StringUtils
import top.bettercode.summer.tools.weixin.support.WechatToken
import java.io.UnsupportedEncodingException
import java.net.URLEncoder

/**
 * @author Peter Wu
 */
interface IOffiaccountProperties : IWexinProperties {
    val userUnionid: Boolean
        get() = false
    val appBaseUrl: String?
    val wechatBaseUrl: String?
    val wechatWebOauthUrl: String
    val oauthUrl: String?
        get() = appBaseUrl + OAUTH_URL

    fun redirectUrl(wechatToken: WechatToken?, forceLogin: Boolean, state: String?): String {
        return "redirect:" + wechatUrl(wechatToken, forceLogin, state)
    }

    fun wechatUrl(wechatToken: WechatToken?, forceLogin: Boolean, state: String?): String {
        val token = wechatToken?.accessToken ?: ""
        val openId = wechatToken?.openId ?: ""
        val msg = wechatToken?.msg ?: ""
        val encodeMsg: String
        encodeMsg = try {
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
        val log = LoggerFactory.getLogger(IOffiaccountProperties::class.java)
        const val OAUTH_URL = "/wechat/oauth"
    }
}
