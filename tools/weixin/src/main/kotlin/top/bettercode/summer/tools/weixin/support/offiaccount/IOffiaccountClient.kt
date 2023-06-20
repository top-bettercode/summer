package top.bettercode.summer.tools.weixin.support.offiaccount

import top.bettercode.summer.tools.weixin.properties.IOffiaccountProperties
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

    fun <T> sendTemplateMsg(request: TemplateMsgRequest<T>): MsgResult

    fun <T> sendTemplateMsg(request: TemplateMsgRequest<T>, retries: Int): MsgResult

    //--------------------------------------------

    fun jsSignUrl(url: String): JsapiSignature

    fun shaHex(vararg str: String): String

}