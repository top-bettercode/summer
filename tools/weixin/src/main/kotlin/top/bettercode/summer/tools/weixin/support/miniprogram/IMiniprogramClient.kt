package top.bettercode.summer.tools.weixin.support.miniprogram

import top.bettercode.summer.tools.weixin.support.WeixinResponse
import top.bettercode.summer.tools.weixin.support.miniprogram.entity.JsSession
import top.bettercode.summer.tools.weixin.support.miniprogram.entity.PhoneInfoResp
import top.bettercode.summer.tools.weixin.support.miniprogram.entity.SubscribeMsgRequest

/**
 *
 * @author Peter Wu
 */
interface IMiniprogramClient {

    fun jscode2session(code: String): JsSession

    fun getuserphonenumber(code: String): PhoneInfoResp

    fun getuserphonenumber(code: String, retries: Int): PhoneInfoResp

    /**
     * https://developers.weixin.qq.com/miniprogram/dev/OpenApiDoc/mp-message-management/subscribe-message/sendMessage.html
     */
    fun <T> sendSubscribeMsg(request: SubscribeMsgRequest<T>): WeixinResponse

    /**
     * https://developers.weixin.qq.com/miniprogram/dev/OpenApiDoc/mp-message-management/subscribe-message/sendMessage.html
     */
    fun <T> sendSubscribeMsg(request: SubscribeMsgRequest<T>, retries: Int): WeixinResponse
}