package top.bettercode.summer.tools.weixin.support.miniprogram

import org.springframework.web.client.getForObject
import org.springframework.web.client.postForObject
import top.bettercode.summer.logging.annotation.LogMarker
import top.bettercode.summer.tools.weixin.properties.MiniprogramProperties
import top.bettercode.summer.tools.weixin.support.IWeixinCache
import top.bettercode.summer.tools.weixin.support.WeixinClient
import top.bettercode.summer.tools.weixin.support.WeixinResponse
import top.bettercode.summer.tools.weixin.support.miniprogram.entity.JsSession
import top.bettercode.summer.tools.weixin.support.miniprogram.entity.PhoneInfoResp
import top.bettercode.summer.tools.weixin.support.miniprogram.entity.SubscribeMsgRequest
import top.bettercode.summer.tools.weixin.support.miniprogram.entity.UniformMsgRequest

/**
 *
 * @author Peter Wu
 */
@LogMarker(MiniprogramClient.MARKER)
open class MiniprogramClient(
    properties: MiniprogramProperties,
    cache: IWeixinCache
) :
    WeixinClient<MiniprogramProperties>(
        properties,
        cache,
        MARKER
    ) {

    companion object {
        const val MARKER = "wxmini"
    }

    open fun jscode2session(code: String): JsSession {
        val session = getForObject<JsSession>(
            "https://api.weixin.qq.com/sns/jscode2session?appid={0}&secret={1}&js_code={1}&grant_type=authorization_code",
            properties.appId,
            properties.secret,
            code
        )
        return if (session.isOk) {
            session
        } else {
            throw clientException("获取session失败：${session.errmsg}", session)
        }
    }

    @JvmOverloads
    open fun getuserphonenumber(code: String, retries: Int = 1): PhoneInfoResp {
        val result = postForObject<PhoneInfoResp>(
            "https://api.weixin.qq.com/wxa/business/getuserphonenumber?access_token={0}",
            mapOf("code" to code),
            getStableAccessToken()
        )
        return if (result.isOk) {
            result
        } else if (40001 == result.errcode || 42001 == result.errcode) {
            //40001 access_token无效
            //42001 access_token过期
            clearStableTokenCache()
            getuserphonenumber(code, retries)
        } else if (retries < properties.maxRetries) {
            getuserphonenumber(code, retries + 1)
        } else {
            throw clientException("手机号授权失败：${result.errmsg}", result)
        }
    }

    /**
     * https://developers.weixin.qq.com/miniprogram/dev/OpenApiDoc/mp-message-management/subscribe-message/sendMessage.html
     */
    @JvmOverloads
    open fun sendSubscribeMsg(request: SubscribeMsgRequest, retries: Int = 1): WeixinResponse {
        if (request.miniprogramState == null) {
            request.miniprogramState = properties.miniprogramState
        }
        val result = postForObject<WeixinResponse>(
            "https://api.weixin.qq.com/cgi-bin/message/subscribe/send?access_token={0}",
            request,
            getStableAccessToken()
        )
        return if (result.isOk) {
            result
        } else if (40001 == result.errcode || 42001 == result.errcode) {
            //40001 access_token无效
            //42001 access_token过期
            clearStableTokenCache()
            sendSubscribeMsg(request, retries)
        } else if (retries < properties.maxRetries) {
            sendSubscribeMsg(request, retries + 1)
        } else if (43101 == result.errcode) {
            //43101 用户未订阅消息
            log.warn("发送订阅消息失败：errcode:${result.errcode},errmsg:${result.errmsg}")
            result
        } else {
            throw clientException("发送订阅消息失败：${result.errmsg}", result)
        }
    }

    /**
     * 发送统一服务消息
     * https://developers.weixin.qq.com/miniprogram/dev/OpenApiDoc/mp-message-management/uniform-message/sendUniformMessage.html
     */
    @Suppress("DEPRECATION")
    @Deprecated(
        "自 2023 年 9 月 20 日起（以下简称 “生效期”），下发统一消息接口将被收回。",
        replaceWith = ReplaceWith(
            "offiaccountClient.sendTemplateMsg(TemplateMsgRequest(), 1)",
            "top.bettercode.summer.tools.weixin.support.offiaccount.OffiaccountClient",
            "top.bettercode.summer.tools.weixin.support.offiaccount.entity.TemplateMsgRequest"
        )
    )
    @JvmOverloads
    open fun sendUniformMsg(request: UniformMsgRequest, retries: Int = 1): WeixinResponse {
        val result = postForObject<WeixinResponse>(
            "https://api.weixin.qq.com/cgi-bin/message/wxopen/template/uniform_send?access_token={0}",
            request,
            getStableAccessToken()
        )
        return if (result.isOk) {
            result
        } else if (40001 == result.errcode || 42001 == result.errcode) {
            //40001 access_token无效
            //42001 access_token过期
            clearStableTokenCache()
            sendUniformMsg(request, retries)
        } else if (43004 == result.errcode) {
            //43004 需要接收者关注
            log.warn("发送模板消息失败：errcode:${result.errcode},errmsg:${result.errmsg}")
            result
        } else if (retries < properties.maxRetries) {
            sendUniformMsg(request, retries + 1)
        } else {
            throw clientException("发送统一服务消息失败：${result.errmsg}", result)
        }
    }

}