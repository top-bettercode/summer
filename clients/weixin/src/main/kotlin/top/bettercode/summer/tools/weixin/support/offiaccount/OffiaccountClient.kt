package top.bettercode.summer.tools.weixin.support.offiaccount

import org.slf4j.MarkerFactory
import org.springframework.web.client.getForObject
import org.springframework.web.client.postForObject
import top.bettercode.summer.logging.annotation.LogMarker
import top.bettercode.summer.tools.lang.util.Sha1DigestUtil
import top.bettercode.summer.tools.weixin.properties.OffiaccountProperties
import top.bettercode.summer.tools.weixin.support.IWeixinCache
import top.bettercode.summer.tools.weixin.support.WeixinClient
import top.bettercode.summer.tools.weixin.support.WeixinResponse
import top.bettercode.summer.tools.weixin.support.offiaccount.OffiaccountClient.Companion.LOG_MARKER
import top.bettercode.summer.tools.weixin.support.offiaccount.aes.WXBizMsgCrypt
import top.bettercode.summer.tools.weixin.support.offiaccount.custmsg.CustMsg
import top.bettercode.summer.tools.weixin.support.offiaccount.entity.*
import java.net.URLEncoder
import java.time.Duration
import java.util.*

/**
 * 公众号接口
 *
 * @author Peter Wu
 */
@LogMarker(LOG_MARKER)
open class OffiaccountClient(
    properties: OffiaccountProperties,
    cache: IWeixinCache
) :
    WeixinClient<OffiaccountProperties>(
        properties,
        cache,
        LOG_MARKER
    ) {

    val wxBizMsgCrypt: WXBizMsgCrypt by lazy {
        WXBizMsgCrypt(properties.token, properties.aesKey, properties.appId)
    }

    companion object {
        const val JSAPI_TICKET_KEY: String = "jsapi_ticket:"
        const val LOG_MARKER = "weixin"


    }

    init {
        val url =
            "https://open.weixin.qq.com/connect/oauth2/authorize?appid=%s&redirect_uri=%s&response_type=code&scope=%s&state=#wechat_redirect"
        val authenticationUrl = String.format(
            url,
            properties.appId,
            URLEncoder.encode(properties.oauthUrl, "UTF-8"),
            "snsapi_userinfo"
        )
        log.info(MarkerFactory.getMarker(logMarker), "authenticationUrl:{}", authenticationUrl)
    }

    @JvmOverloads
    open fun getJsapiTicket(retries: Int = 1): String {
        return putIfAbsent(JSAPI_TICKET_KEY + ":" + properties.appId) {
            getTicket(retries)
        }
    }

    private fun getTicket(retries: Int): CachedValue {
        val result = getForObject<JsapiTicket>(
            "https://api.weixin.qq.com/cgi-bin/ticket/getticket?access_token={0}&type=jsapi",
            getStableAccessToken()
        )
        return if (result.isOk) {
            CachedValue(
                result.ticket!!,
                Duration.ofSeconds(result.expiresIn!!.toLong())
            )
        } else if (40001 == result.errcode || 42001 == result.errcode) {
            //40001 access_token无效
            //42001 access_token过期
            clearStableTokenCache()
            getTicket(retries)
        } else if (retries < properties.maxRetries) {
            getTicket(retries + 1)
        } else {
            throw clientException("获取jsapiTicket失败：${result.errmsg}", result)
        }
    }

    open fun getWebPageAccessToken(code: String): WebPageAccessToken {
        return getForObject(
            "https://api.weixin.qq.com/sns/oauth2/access_token?appid={0}&secret={1}&code={2}&grant_type=authorization_code",
            properties.appId,
            properties.secret,
            code
        )
    }

    open fun getSnsapiUserinfo(accessToken: String, openid: String): SnsapiUserinfo {
        return getSnsapiUserinfo(accessToken, openid, "zh_CN")
    }

    /**
     * https://developers.weixin.qq.com/doc/offiaccount/OA_Web_Apps/Wechat_webpage_authorization.html#3
     */
    open fun getSnsapiUserinfo(
        accessToken: String,
        openid: String,
        lang: String,
        retries: Int = 2
    ): SnsapiUserinfo {
        val result = getForObject<SnsapiUserinfo>(
            "https://api.weixin.qq.com/sns/userinfo?access_token={0}&openid={1}&lang={2}",
            accessToken,
            openid,
            lang
        )
        return if (result.isOk) {
            result
        } else if (retries < properties.maxRetries) {
            getSnsapiUserinfo(accessToken, openid, lang, retries + 1)
        } else {
            throw clientException("${result.errmsg}", result)
        }

    }

    /**
     * https://developers.weixin.qq.com/doc/offiaccount/User_Management/Get_users_basic_information_UnionID.html#UinonId
     */
    open fun getUserInfo(openid: String): UserInfo {
        return getUserInfo(openid, "zh_CN")
    }

    open fun getUserInfo(openid: String, lang: String, retries: Int = 1): UserInfo {
        val result = getForObject<UserInfo>(
            "https://api.weixin.qq.com/cgi-bin/user/info?access_token={0}&openid={1}&lang={2}",
            getStableAccessToken(),
            openid,
            lang
        )
        return if (result.isOk) {
            result
        } else if (40001 == result.errcode || 42001 == result.errcode) {
            //40001 access_token无效
            //42001 access_token过期
            clearStableTokenCache()
            getUserInfo(openid, lang)
        } else if (retries < properties.maxRetries) {
            getUserInfo(openid, lang, retries + 1)
        } else {
            throw clientException("${result.errmsg}", result)
        }
    }

    /**
     * https://developers.weixin.qq.com/doc/offiaccount/Message_Management/Template_Message_Interface.html
     */
    @JvmOverloads
    open fun sendTemplateMsg(request: TemplateMsgRequest, retries: Int = 1): MsgResult {
        val result = postForObject<MsgResult>(
            "https://api.weixin.qq.com/cgi-bin/message/template/send?access_token={0}",
            request,
            getStableAccessToken()
        )
        return if (result.isOk) {
            result
        } else if (40001 == result.errcode || 42001 == result.errcode) {
            //40001 access_token无效
            //42001 access_token过期
            clearStableTokenCache()
            sendTemplateMsg(request, retries)
        } else if (retries < properties.maxRetries) {
            sendTemplateMsg(request, retries + 1)
        } else if (43004 == result.errcode || 43101 == result.errcode) {
            //43004 需要接收者关注
            //43101 用户未订阅消息
            log.warn("发送模板消息失败：errcode:${result.errcode},errmsg:${result.errmsg}")
            result
        } else {
            throw clientException("发送模板消息失败：${result.errmsg}", result)
        }
    }

    //--------------------------------------------

    open fun jsSignUrl(url: String): JsapiSignature {
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

    /**
     * 客服接口-发消息
     * https://developers.weixin.qq.com/doc/offiaccount/Message_Management/Service_Center_messages.html#%E5%AE%A2%E6%9C%8D%E6%8E%A5%E5%8F%A3-%E5%8F%91%E6%B6%88%E6%81%AF
     */
    @JvmOverloads
    open fun sendCustomMessage(request: CustMsg, retries: Int = 1): WeixinResponse {
        val result = postForObject<WeixinResponse>(
            "https://api.weixin.qq.com/cgi-bin/message/custom/send?access_token={0}",
            request,
            getStableAccessToken()
        )
        return if (result.isOk) {
            result
        } else if (40001 == result.errcode || 42001 == result.errcode) {
            //40001 access_token无效
            //42001 access_token过期
            clearStableTokenCache()
            sendCustomMessage(request, retries)
        } else if (40241 == result.errcode) {
            //40241 The message is duplicate in 60 seconds
            log.warn("发送模板消息失败：errcode:${result.errcode},errmsg:${result.errmsg}")
            result
        } else if (retries < properties.maxRetries) {
            sendCustomMessage(request, retries + 1)
        } else {
            throw clientException("发送客服消息失败：${result.errmsg}", result)
        }
    }
}