package top.bettercode.summer.tools.weixin.support.offiaccount

import org.slf4j.MarkerFactory
import org.springframework.web.client.getForObject
import org.springframework.web.client.postForObject
import top.bettercode.summer.logging.annotation.LogMarker
import top.bettercode.summer.tools.lang.util.Sha1DigestUtil
import top.bettercode.summer.tools.weixin.properties.IOffiaccountProperties
import top.bettercode.summer.tools.weixin.support.WeixinClient
import top.bettercode.summer.tools.weixin.support.WeixinException
import top.bettercode.summer.tools.weixin.support.offiaccount.OffiaccountClient.Companion.LOG_MARKER
import top.bettercode.summer.tools.weixin.support.offiaccount.entity.*
import java.net.URLEncoder
import java.time.LocalDateTime
import java.util.*

/**
 * 公众号接口
 *
 * @author Peter Wu
 */
@LogMarker(LOG_MARKER)
open class OffiaccountClient(properties: IOffiaccountProperties) :
        WeixinClient<IOffiaccountProperties>(
                properties,
                "第三方平台",
                "微信公众号",
                LOG_MARKER
        ), IOffiaccountClient {

    companion object {
        const val JSAPI_TICKET_KEY: String = "jsapi_ticket:"
        const val LOG_MARKER = "weixin"
    }

    init {
        val url =
                "https://open.weixin.qq.com/connect/oauth2/authorize?appid=%s&redirect_uri=%s&response_type=code&scope=%s#wechat_redirect"
        val authenticationUrl = String.format(
                url,
                properties.appId,
                URLEncoder.encode(properties.oauthUrl, "UTF-8"),
                "snsapi_userinfo"
        )
        log.info(MarkerFactory.getMarker(logMarker), "authenticationUrl:{}", authenticationUrl)
    }

    override fun getJsapiTicket(): String {
        return getJsapiTicket(1)
    }

    override fun getJsapiTicket(retries: Int): String {
        return putIfAbsent(JSAPI_TICKET_KEY + ":" + properties.appId) {
            getTicket(retries)
        }
    }

    private fun getTicket(retries: Int): CachedValue {
        val jsapiTicket = getForObject<JsapiTicket>(
                "https://api.weixin.qq.com/cgi-bin/ticket/getticket?access_token={0}&type=jsapi",
                getBaseAccessToken()
        )
        return if (jsapiTicket.isOk) {
            CachedValue(
                    jsapiTicket.ticket!!,
                    LocalDateTime.now().plusSeconds(jsapiTicket.expiresIn!!.toLong())
            )
        } else if (40001 == jsapiTicket.errcode) {
            clearCache()
            getTicket(retries)
        } else if (retries < properties.maxRetries) {
            getTicket(retries + 1)
        } else {
            throw WeixinException("获取jsapiTicket失败：${jsapiTicket.errmsg}", jsapiTicket)
        }
    }

    override fun getWebPageAccessToken(code: String): WebPageAccessToken {
        return getForObject(
                "https://api.weixin.qq.com/sns/oauth2/access_token?appid={0}&secret={1}&code={2}&grant_type=authorization_code",
                properties.appId,
                properties.secret,
                code
        )
    }

    override fun getSnsapiUserinfo(accessToken: String, openid: String): SnsapiUserinfo {
        return getSnsapiUserinfo(accessToken, openid, "zh_CN")
    }

    /**
     * https://developers.weixin.qq.com/doc/offiaccount/OA_Web_Apps/Wechat_webpage_authorization.html#3
     */
    override fun getSnsapiUserinfo(
            accessToken: String,
            openid: String,
            lang: String
    ): SnsapiUserinfo {
        return getForObject(
                "https://api.weixin.qq.com/sns/userinfo?access_token={0}&openid={1}&lang={2}",
                accessToken,
                openid,
                lang
        )
    }

    override fun getUserInfo(openid: String): UserInfo {
        return getUserInfo(openid, "zh_CN")
    }

    override fun getUserInfo(openid: String, lang: String): UserInfo {
        return getForObject("https://api.weixin.qq.com/cgi-bin/user/info?access_token={0}&openid={1}&lang={2}", getBaseAccessToken(), openid, lang)
    }

    override fun sendTemplateMsg(request: TemplateMsgRequest): MsgResult {
        return sendTemplateMsg(request, 1)
    }

    /**
     * https://developers.weixin.qq.com/doc/offiaccount/Message_Management/Template_Message_Interface.html
     */
    override fun sendTemplateMsg(request: TemplateMsgRequest, retries: Int): MsgResult {
        val result = postForObject<MsgResult>(
                "https://api.weixin.qq.com/cgi-bin/message/template/send?access_token={0}",
                request,
                getBaseAccessToken()
        )
        return if (result.isOk) {
            result
        } else if (40001 == result.errcode) {
            //40001 access_token无效
            clearCache()
            sendTemplateMsg(request, retries)
        } else if (retries < properties.maxRetries) {
            sendTemplateMsg(request, retries + 1)
        } else if (43004 == result.errcode) {
            //43004 需要接收者关注
            log.warn("发送模板消息失败：errcode:${result.errcode},errmsg:${result.errmsg}")
            result
        } else {
            throw WeixinException("发送模板消息失败：${result.errmsg}", result)
        }
    }

    //--------------------------------------------

    override fun jsSignUrl(url: String): JsapiSignature {
        val nonceStr = UUID.randomUUID().toString()
        val timestamp = (System.currentTimeMillis() / 1000).toString()
        val jsapiTicket = getJsapiTicket()
        //注意这里参数名必须全部小写，且必须有序
        val signature = Sha1DigestUtil.shaHex(
                "jsapi_ticket=" + jsapiTicket +
                        "&noncestr=" + nonceStr +
                        "&timestamp=" + timestamp +
                        "&url=" + url
        )

        return JsapiSignature(signature, properties.appId, nonceStr, timestamp)
    }

    override fun shaHex(vararg str: String): String {
        str.sort()
        return Sha1DigestUtil.shaHex(str.joinToString(""))
    }

}