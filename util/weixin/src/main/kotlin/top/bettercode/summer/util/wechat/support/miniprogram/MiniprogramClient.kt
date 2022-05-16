package top.bettercode.summer.util.wechat.support.miniprogram

import org.springframework.web.client.getForObject
import org.springframework.web.client.postForObject
import top.bettercode.summer.util.wechat.config.IMiniprogramProperties
import top.bettercode.summer.util.wechat.support.WeixinClient
import top.bettercode.summer.util.wechat.support.WeixinResponse
import top.bettercode.summer.util.wechat.support.miniprogram.entity.JsSession
import top.bettercode.summer.util.wechat.support.miniprogram.entity.PhoneInfoResp
import top.bettercode.summer.util.wechat.support.miniprogram.entity.SubscribeMsgRequest

/**
 *
 * @author Peter Wu
 */
class MiniprogramClient(properties: IMiniprogramProperties) :
    WeixinClient<IMiniprogramProperties>(
        properties,
        "第三方接口",
        "微信公众号",
        "wexin-offiaccount"
    ), IMiniprogramClient {

    override fun jscode2session(code: String): JsSession {
        return getForObject(
            "https://api.weixin.qq.com/sns/jscode2session?appid={0}&secret={1}&js_code={1}&grant_type=authorization_code",
            getAppId(),
            getSecret(),
            code
        )
    }

    override fun getuserphonenumber(code: String): PhoneInfoResp {
        return postForObject(
            "https://api.weixin.qq.com/wxa/business/getuserphonenumber?access_token={0}",
            mapOf("code" to code),
            getBaseAccessToken()
        )
    }

    override fun sendSubscribeMsg(request: SubscribeMsgRequest): WeixinResponse {
        return sendSubscribeMsg(request, 1)
    }

    override fun sendSubscribeMsg(request: SubscribeMsgRequest, retries: Int): WeixinResponse {
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
        } else {
            throw RuntimeException("发送订阅消息失败：errcode:${result.errcode},errmsg:${result.errmsg}")
        }
    }

}