package top.bettercode.summer.tools.weixin.support.miniprogram

import top.bettercode.summer.tools.weixin.support.WeixinResponse
import top.bettercode.summer.tools.weixin.support.miniprogram.entity.JsSession
import top.bettercode.summer.tools.weixin.support.miniprogram.entity.PhoneInfoResp
import top.bettercode.summer.tools.weixin.support.miniprogram.entity.SubscribeMsgRequest
import top.bettercode.summer.tools.weixin.support.miniprogram.entity.UniformMsgRequest

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
    fun sendSubscribeMsg(request: SubscribeMsgRequest): WeixinResponse

    /**
     * https://developers.weixin.qq.com/miniprogram/dev/OpenApiDoc/mp-message-management/subscribe-message/sendMessage.html
     */
    fun sendSubscribeMsg(request: SubscribeMsgRequest, retries: Int): WeixinResponse

    /**
     * 发送统一服务消息
     * https://developers.weixin.qq.com/miniprogram/dev/OpenApiDoc/mp-message-management/uniform-message/sendUniformMessage.html
     */
    fun sendUniformMsg(request: UniformMsgRequest): WeixinResponse

    /**
     * 发送统一服务消息
     * https://developers.weixin.qq.com/miniprogram/dev/OpenApiDoc/mp-message-management/uniform-message/sendUniformMessage.html
     */
    fun sendUniformMsg(request: UniformMsgRequest, retries: Int): WeixinResponse
}