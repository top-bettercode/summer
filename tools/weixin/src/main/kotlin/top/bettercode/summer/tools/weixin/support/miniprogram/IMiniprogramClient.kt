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

    fun sendSubscribeMsg(request: SubscribeMsgRequest): WeixinResponse

    fun sendSubscribeMsg(request: SubscribeMsgRequest, retries: Int): WeixinResponse
}