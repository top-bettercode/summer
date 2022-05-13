package top.bettercode.summer.util.wechat.support.miniprogram

import org.springframework.web.client.getForObject
import org.springframework.web.client.postForObject
import top.bettercode.summer.util.wechat.config.IMiniprogramProperties
import top.bettercode.summer.util.wechat.config.MiniprogramProperties
import top.bettercode.summer.util.wechat.support.WeixinClient
import top.bettercode.summer.util.wechat.support.WeixinClient.Companion.baseAccessTokenKey
import top.bettercode.summer.util.wechat.support.WeixinClient.Companion.maxRetries
import top.bettercode.summer.util.wechat.support.WeixinResponse
import top.bettercode.summer.util.wechat.support.miniprogram.entity.JsSession
import top.bettercode.summer.util.wechat.support.miniprogram.entity.PhoneInfoResp
import top.bettercode.summer.util.wechat.support.miniprogram.entity.SubscribeMsgRequest

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