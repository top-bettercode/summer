package top.bettercode.summer.util.wechat.support.offiaccount

import org.slf4j.MarkerFactory
import org.springframework.web.client.getForObject
import org.springframework.web.client.postForObject
import top.bettercode.lang.util.Sha1DigestUtil
import top.bettercode.summer.util.wechat.config.IOffiaccountProperties
import top.bettercode.summer.util.wechat.config.OffiaccountProperties
import top.bettercode.summer.util.wechat.support.WeixinClient
import top.bettercode.summer.util.wechat.support.WeixinClient.Companion.baseAccessTokenKey
import top.bettercode.summer.util.wechat.support.WeixinClient.Companion.maxRetries
import top.bettercode.summer.util.wechat.support.offiaccount.OffiaccountClient.Companion.jsapiTicketKey
import top.bettercode.summer.util.wechat.support.offiaccount.entity.*
import java.net.URLEncoder
import java.time.LocalDateTime
import java.util.*

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