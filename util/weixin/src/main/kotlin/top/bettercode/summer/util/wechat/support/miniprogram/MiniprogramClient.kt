package top.bettercode.summer.util.wechat.support.miniprogram

import org.springframework.web.client.getForObject
import org.springframework.web.client.postForObject
import top.bettercode.summer.util.wechat.config.MiniprogramProperties
import top.bettercode.summer.util.wechat.support.WeixinClient
import top.bettercode.summer.util.wechat.support.WeixinResponse
import top.bettercode.summer.util.wechat.support.miniprogram.entity.JsSession
import top.bettercode.summer.util.wechat.support.miniprogram.entity.SubscribeMsgRequest

/**
 *
 * @author Peter Wu
 */
class MiniprogramClient(properties: MiniprogramProperties) :
    WeixinClient<MiniprogramProperties>(
        properties,
        "第三方接口",
        "微信公众号",
        "wexin-offiaccount"
    ) {

    fun code2Session(code: String): JsSession {
        return getForObject(
            "https://api.weixin.qq.com/sns/jscode2session?appid={0}&secret={1}&js_code={1}&grant_type=authorization_code",
            properties.appId,
            properties.secret,
            code
        )
    }

    fun sendSubscribeMsg(request: SubscribeMsgRequest): WeixinResponse {
        return postForObject(
            "https://api.weixin.qq.com/cgi-bin/message/subscribe/send?access_token={0}",
            request,
            getBaseAccessToken()
        )
    }

}