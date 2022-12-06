package top.bettercode.summer.tools.weixin.support.offiaccount

import org.slf4j.MarkerFactory
import org.springframework.web.client.getForObject
import org.springframework.web.client.postForObject
import top.bettercode.summer.tools.lang.util.Sha1DigestUtil
import top.bettercode.summer.tools.weixin.config.IOffiaccountProperties
import top.bettercode.summer.tools.weixin.support.WeixinClient
import top.bettercode.summer.tools.weixin.support.offiaccount.entity.*
import java.net.URLEncoder
import java.time.LocalDateTime
import java.util.*

/**
 * 公众号接口
 *
 * @author Peter Wu
 */
class OffiaccountClient(properties: IOffiaccountProperties) :
    WeixinClient<IOffiaccountProperties>(
        properties,
        "第三方接口",
        "微信公众号",
        "weixin"
    ), IOffiaccountClient {

    companion object {
        const val jsapiTicketKey: String = "jsapi_ticket"
    }

    init {
        val url =
            "https://open.weixin.qq.com/connect/oauth2/authorize?appid=%s&redirect_uri=%s&response_type=code&scope=%s#wechat_redirect"
        val authenticationUrl = String.format(
            url,
            getAppId(),
            URLEncoder.encode(properties.oauthUrl, "UTF-8"),
            "snsapi_userinfo"
        )
        log.info(MarkerFactory.getMarker(logMarker), "authenticationUrl:{}", authenticationUrl)
    }

    override fun getJsapiTicket(): String {
        return getJsapiTicket(1)
    }

    override fun getJsapiTicket(retries: Int): String {
        val cachedValue = cache.getIfPresent(jsapiTicketKey)
        return if (cachedValue == null || cachedValue.expired) {
            val jsapiTicket = getForObject<JsapiTicket>(
                "https://api.weixin.qq.com/cgi-bin/ticket/getticket?access_token={0}&type=jsapi",
                getBaseAccessToken()
            )
            if (jsapiTicket.isOk) {
                cache.put(
                    jsapiTicketKey,
                    CachedValue(
                        jsapiTicket.ticket!!,
                        LocalDateTime.now().plusSeconds(jsapiTicket.expiresIn!!.toLong())
                    )
                )
                jsapiTicket.ticket
            } else if (40001 == jsapiTicket.errcode) {
                cache.invalidate(baseAccessTokenKey)
                getJsapiTicket(retries)
            } else if (retries < properties.maxRetries) {
                getJsapiTicket(retries + 1)
            } else {
                throw RuntimeException("获取jsapiTicket失败：errcode:${jsapiTicket.errcode},errmsg:${jsapiTicket.errmsg}")
            }
        } else {
            cachedValue.value
        }
    }

    override fun getWebPageAccessToken(code: String): WebPageAccessToken {
        return getForObject(
            "https://api.weixin.qq.com/sns/oauth2/access_token?appid={0}&secret={1}&code={2}&grant_type=authorization_code",
            getAppId(),
            getSecret(),
            code
        )
    }

    override fun getSnsapiUserinfo(accessToken: String, openid: String): SnsapiUserinfo {
        return getSnsapiUserinfo(openid, "zh_CN")
    }

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

    override fun sendTemplateMsg(request: TemplateMsgRequest): MsgResult {
        return sendTemplateMsg(request, 1)
    }

    override fun sendTemplateMsg(request: TemplateMsgRequest, retries: Int): MsgResult {
        val result = postForObject<MsgResult>(
            "https://api.weixin.qq.com/cgi-bin/message/template/send?access_token={0}",
            request,
            getBaseAccessToken()
        )
        return if (result.isOk) {
            result
        } else if (40001 == result.errcode) {
            cache.invalidate(baseAccessTokenKey)
            sendTemplateMsg(request, retries)
        } else if (retries < properties.maxRetries) {
            sendTemplateMsg(request, retries + 1)
        } else if (43004 == result.errcode) {
            log.warn("发送模板消息失败：errcode:${result.errcode},errmsg:${result.errmsg}")
            result
        } else {
            throw RuntimeException("发送模板消息失败：errcode:${result.errcode},errmsg:${result.errmsg}")
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

        return JsapiSignature(signature, getAppId(), nonceStr, timestamp)
    }

    override fun shaHex(vararg str: String): String {
        str.sort()
        return Sha1DigestUtil.shaHex(str.joinToString(""))
    }

}