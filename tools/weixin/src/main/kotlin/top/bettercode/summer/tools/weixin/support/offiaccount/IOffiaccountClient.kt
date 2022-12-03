package top.bettercode.summer.tools.weixin.support.offiaccount

import top.bettercode.summer.tools.weixin.config.IOffiaccountProperties
import top.bettercode.summer.tools.weixin.support.offiaccount.entity.*

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

    fun getSnsapiUserinfo(accessToken: String, openid: String): SnsapiUserinfo

    fun getSnsapiUserinfo(accessToken: String, openid: String, lang: String): SnsapiUserinfo

    fun sendTemplateMsg(request: TemplateMsgRequest): MsgResult

    fun sendTemplateMsg(request: TemplateMsgRequest, retries: Int): MsgResult

    //--------------------------------------------

    fun jsSignUrl(url: String): JsapiSignature

    fun shaHex(vararg str: String): String

}