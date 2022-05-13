package top.bettercode.summer.util.wechat.support.offiaccount

import top.bettercode.summer.util.wechat.config.IOffiaccountProperties
import top.bettercode.summer.util.wechat.support.offiaccount.entity.JsapiSignature
import top.bettercode.summer.util.wechat.support.offiaccount.entity.MsgResult
import top.bettercode.summer.util.wechat.support.offiaccount.entity.TemplateMsgRequest
import top.bettercode.summer.util.wechat.support.offiaccount.entity.WebPageAccessToken

/**
 * 公众号接口
 *
 * @author Peter Wu
 */
interface IOffiaccountClient {

    val properties: IOffiaccountProperties

    fun getJsapiTicket(): String

    fun getJsapiTicket(retries: Int): String

    fun getWebPageAccessToken(code: String): WebPageAccessToken

    fun sendTemplateMsg(request: TemplateMsgRequest): MsgResult

    fun sendTemplateMsg(request: TemplateMsgRequest, retries: Int): MsgResult

    //--------------------------------------------

    fun jsSignUrl(url: String): JsapiSignature

    fun shaHex(vararg str: String): String

}