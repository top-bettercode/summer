package top.bettercode.summer.tools.weixin.controller

import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseBody
import top.bettercode.summer.logging.annotation.RequestLogging
import top.bettercode.summer.security.authorize.Anonymous
import top.bettercode.summer.security.authorize.ClientAuthorize
import top.bettercode.summer.tools.weixin.support.IWechatService
import top.bettercode.summer.tools.weixin.support.WechatToken
import top.bettercode.summer.tools.weixin.support.offiaccount.IOffiaccountClient
import top.bettercode.summer.web.BaseController
import javax.validation.constraints.NotBlank

@ConditionalOnWebApplication
@Controller
@Anonymous
@RequestMapping(value = ["/wechat"], name = "微信")
class OffiaccountCallbackController(
        private val wechatService: IWechatService,
        private val offiaccountClient: IOffiaccountClient
) : BaseController() {

    /*
   * 公众号OAuth回调接口
   */
    @RequestLogging(ignoredTimeout = true)
    @GetMapping(value = ["/oauth"], name = "公众号OAuth回调接口")
    fun oauth(code: String?, state: String?): String {
        plainTextError()
        val token: WechatToken =
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
                            WechatToken(e.message)
                        }
                    else WechatToken(accessToken?.errmsg)
                    token.openId = accessToken?.openid ?: ""
                    token
                } catch (e: Exception) {
                    log.warn("token获取失败", e)
                    WechatToken(e.message)
                }
        return offiaccountClient.properties.redirectUrl(token, wechatService.forceLogin(), state)
    }

    /*
   * js签名
   */
    @ClientAuthorize
    @ResponseBody
    @GetMapping(value = ["/jsSign"], name = "公众号js签名")
    fun jsSignUrl(@NotBlank url: String): Any {
        return ok(offiaccountClient.jsSignUrl(url))
    }

    @ResponseBody
    @GetMapping(name = "公众号验证回调")
    fun access(signature: String?, echostr: String?, timestamp: String?, nonce: String?): Any? {
        if (timestamp.isNullOrBlank() || nonce.isNullOrBlank() || offiaccountClient
                        .shaHex(offiaccountClient.properties.token, timestamp, nonce) != signature
        ) {
            log.warn("非法请求.")
            return false
        }
        return echostr
    }

    @ResponseBody
    @PostMapping(name = "公众号事件推送")
    fun receive(
            signature: String?, timestamp: String?,
            nonce: String?, openid: String?, encrypt_type: String?, msg_signature: String?,
            content: String?
    ): String? {
        if (timestamp.isNullOrBlank() || nonce.isNullOrBlank() || openid.isNullOrBlank() || encrypt_type.isNullOrBlank() || msg_signature.isNullOrBlank() || content.isNullOrBlank()
                || offiaccountClient.shaHex(
                        offiaccountClient.properties.token,
                        timestamp,
                        nonce
                ) != signature
        ) {
            log.warn("非法请求.")
        } else {
            wechatService.receive(
                    timestamp,
                    nonce,
                    openid,
                    encrypt_type,
                    msg_signature,
                    content
            )
        }
        return null
    }
}