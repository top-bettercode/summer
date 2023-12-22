package top.bettercode.summer.tools.weixin.controller

import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.*
import top.bettercode.summer.logging.annotation.RequestLogging
import top.bettercode.summer.security.authorize.Anonymous
import top.bettercode.summer.tools.lang.util.XmlMapperUtil
import top.bettercode.summer.tools.weixin.support.IDuplicatedMessageChecker
import top.bettercode.summer.tools.weixin.support.IWeixinService
import top.bettercode.summer.tools.weixin.support.WeixinToken
import top.bettercode.summer.tools.weixin.support.offiaccount.OffiaccountClient
import top.bettercode.summer.tools.weixin.support.offiaccount.aes.DecryptMsg
import top.bettercode.summer.tools.weixin.support.offiaccount.aes.EncryptMsg
import top.bettercode.summer.tools.weixin.support.offiaccount.aes.WXBizMsgCrypt
import top.bettercode.summer.web.BaseController
import javax.validation.constraints.NotBlank

@ConditionalOnWebApplication
@Controller
@Anonymous
@RequestMapping(value = ["/wechat"], name = "微信")
class OffiaccountCallbackController(
        private val wechatService: IWeixinService,
        private val offiaccountClient: OffiaccountClient,
        private val duplicatedMessageChecker: IDuplicatedMessageChecker
) : BaseController() {


    /*
   * 公众号OAuth回调接口
   */
    @RequestLogging(ignoredTimeout = true)
    @GetMapping(value = ["/oauth"], name = "公众号OAuth回调接口")
    fun oauth(code: String?, state: String?): String {
        plainTextError()
        val token: WeixinToken =
                try {
                    val accessToken =
                            if (code.isNullOrBlank()) null else offiaccountClient.getWebPageAccessToken(code)
                    val token = if (accessToken?.isOk == true)
                        try {
                            wechatService.oauth(
                                    accessToken,
                                    (if (offiaccountClient.properties.useUnionid) offiaccountClient.getSnsapiUserinfo(
                                            accessToken.accessToken!!,
                                            accessToken.openid!!
                                    ) else null)
                            )
                        } catch (e: Exception) {
                            log.warn(e.message, e)
                            WeixinToken(e.message)
                        }
                    else WeixinToken(accessToken?.errmsg)
                    token.openId = accessToken?.openid
                    token
                } catch (e: Exception) {
                    log.warn("token获取失败", e)
                    WeixinToken(e.message)
                }
        return offiaccountClient.properties.redirectUrl(token, wechatService.forceLogin(), state)
    }

    /*
   * js签名
   */
//    @ClientAuthorize
    @ResponseBody
    @GetMapping(value = ["/jsSign"], name = "公众号js签名")
    fun jsSignUrl(@NotBlank url: String): Any {
        return ok(offiaccountClient.jsSignUrl(url))
    }

    @ResponseBody
    @GetMapping(name = "公众号验证回调")
    fun access(signature: String, echostr: String?, timestamp: String?, nonce: String?): Any? {
        if (timestamp.isNullOrBlank() || nonce.isNullOrBlank() || WXBizMsgCrypt.shaHex(offiaccountClient.properties.token, timestamp, nonce) != signature
        ) {
            log.warn("非法请求.")
            return false
        }
        return echostr
    }

    /**
     *消息加解密说明:
     * https://developers.weixin.qq.com/doc/oplatform/Third-party_Platforms/2.0/api/Before_Develop/Message_encryption_and_decryption.html
     *接收事件推送:
     * https://developers.weixin.qq.com/doc/offiaccount/Message_Management/Receiving_event_pushes.html
     * 被动回复用户消息:
     * https://developers.weixin.qq.com/doc/offiaccount/Message_Management/Passive_user_reply_message.html
     */
    @Suppress("LocalVariableName")
    @ResponseBody
    @PostMapping(name = "公众号事件推送")
    fun receive(
            signature: String?, timestamp: String?,
            nonce: String?, openid: String?, encrypt_type: String?, msg_signature: String?,
            @RequestBody content: EncryptMsg?
    ): Any? {
        if (timestamp.isNullOrBlank() || nonce.isNullOrBlank() || openid.isNullOrBlank() || encrypt_type.isNullOrBlank() || msg_signature.isNullOrBlank() || content == null
                || content.encrypt.isNullOrBlank()
                || WXBizMsgCrypt.shaHex(offiaccountClient.properties.token, timestamp, nonce, content.encrypt) != signature
        ) {
            log.warn("非法请求.")
        } else {
            val decryptMsg = XmlMapperUtil.fromXml(offiaccountClient.wxBizMsgCrypt.decrypt(content.encrypt), DecryptMsg::class.java)
            if (!duplicatedMessageChecker.isDuplicated(decryptMsg.fromUserName + decryptMsg.createTime)) {
                try {
                    return wechatService.receive(decryptMsg)
                } catch (e: Exception) {
                    log.error(e.message, e)
                }
            }
        }
        return "success"
    }
}