package top.bettercode.summer.tools.weixin.support.offiaccount

import top.bettercode.summer.tools.lang.util.Sha1DigestUtil
import top.bettercode.summer.tools.weixin.properties.IOffiaccountProperties
import top.bettercode.summer.tools.weixin.support.offiaccount.aes.WXBizMsgCrypt
import top.bettercode.summer.tools.weixin.support.offiaccount.entity.*

/**
 * 公众号接口
 *
 * @author Peter Wu
 */
interface IOffiaccountClient {

    val properties: IOffiaccountProperties

    val wxBizMsgCrypt: WXBizMsgCrypt

    fun getJsapiTicket(): String

    fun getJsapiTicket(retries: Int): String

    fun getWebPageAccessToken(code: String): WebPageAccessToken

    fun getSnsapiUserinfo(accessToken: String, openid: String): SnsapiUserinfo

    fun getSnsapiUserinfo(accessToken: String, openid: String, lang: String): SnsapiUserinfo

    /**
     * https://developers.weixin.qq.com/doc/offiaccount/User_Management/Get_users_basic_information_UnionID.html#UinonId
     */
    fun getUserInfo(openid: String): UserInfo

    fun getUserInfo(openid: String, lang: String): UserInfo

    fun sendTemplateMsg(request: TemplateMsgRequest): MsgResult

    fun sendTemplateMsg(request: TemplateMsgRequest, retries: Int): MsgResult

    //--------------------------------------------

    fun jsSignUrl(url: String): JsapiSignature

    companion object {
        @JvmStatic
        fun shaHex(vararg str: String): String {
            str.sort()
            return Sha1DigestUtil.shaHex(str.joinToString(""))
        }

    }
}