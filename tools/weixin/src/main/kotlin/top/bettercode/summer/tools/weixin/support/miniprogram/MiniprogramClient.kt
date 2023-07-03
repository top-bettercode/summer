package top.bettercode.summer.tools.weixin.support.miniprogram

import org.springframework.web.client.getForObject
import org.springframework.web.client.postForObject
import top.bettercode.summer.logging.annotation.LogMarker
import top.bettercode.summer.tools.weixin.properties.IMiniprogramProperties
import top.bettercode.summer.tools.weixin.support.WeixinClient
import top.bettercode.summer.tools.weixin.support.WeixinException
import top.bettercode.summer.tools.weixin.support.WeixinResponse
import top.bettercode.summer.tools.weixin.support.miniprogram.MiniprogramClient.Companion.LOG_MARKER
import top.bettercode.summer.tools.weixin.support.miniprogram.entity.JsSession
import top.bettercode.summer.tools.weixin.support.miniprogram.entity.PhoneInfoResp
import top.bettercode.summer.tools.weixin.support.miniprogram.entity.SubscribeMsgRequest

/**
 *
 * @author Peter Wu
 */
@LogMarker(LOG_MARKER)
class MiniprogramClient(properties: IMiniprogramProperties) :
        WeixinClient<IMiniprogramProperties>(
                properties,
                "第三方平台",
                "微信公众号",
                LOG_MARKER
        ), IMiniprogramClient {

    companion object {
        const val LOG_MARKER = "wxmini"
    }

    override fun jscode2session(code: String): JsSession {
        val session = getForObject<JsSession>(
                "https://api.weixin.qq.com/sns/jscode2session?appid={0}&secret={1}&js_code={1}&grant_type=authorization_code",
                getAppId(),
                getSecret(),
                code
        )
        return if (session.isOk) {
            session
        } else {
            throw WeixinException("获取session失败：errcode:${session.errcode},errmsg:${session.errmsg}", session)
        }
    }

    override fun getuserphonenumber(code: String): PhoneInfoResp {
        return getuserphonenumber(code, 1)
    }

    override fun getuserphonenumber(code: String, retries: Int): PhoneInfoResp {
        val result = postForObject<PhoneInfoResp>(
                "https://api.weixin.qq.com/wxa/business/getuserphonenumber?access_token={0}",
                mapOf("code" to code),
                getBaseAccessToken()
        )
        return if (result.isOk) {
            result
        } else if (40001 == result.errcode) {
            cache.invalidate(baseAccessTokenKey)
            getuserphonenumber(code, retries)
        } else if (retries < properties.maxRetries) {
            getuserphonenumber(code, retries + 1)
        } else {
            throw WeixinException("手机授权失败：errcode:${result.errcode},errmsg:${result.errmsg}", result)
        }
    }

    override fun <T> sendSubscribeMsg(request: SubscribeMsgRequest<T>): WeixinResponse {
        return sendSubscribeMsg(request, 1)
    }

    override fun <T> sendSubscribeMsg(request: SubscribeMsgRequest<T>, retries: Int): WeixinResponse {
        val result = postForObject<WeixinResponse>(
                "https://api.weixin.qq.com/cgi-bin/message/subscribe/send?access_token={0}",
                request,
                getBaseAccessToken()
        )
        return if (result.isOk) {
            result
        } else if (40001 == result.errcode) {
            cache.invalidate(baseAccessTokenKey)
            sendSubscribeMsg(request, retries)
        } else if (retries < properties.maxRetries) {
            sendSubscribeMsg(request, retries + 1)
        } else if (43004 == result.errcode || 43101 == result.errcode) {
            //43101 用户拒绝接收消息
            //43004 需要接收者关注
            log.warn("发送订阅消息失败：errcode:${result.errcode},errmsg:${result.errmsg}")
            result
        } else {
            throw WeixinException("发送订阅消息失败：errcode:${result.errcode},errmsg:${result.errmsg}", result)
        }
    }

}