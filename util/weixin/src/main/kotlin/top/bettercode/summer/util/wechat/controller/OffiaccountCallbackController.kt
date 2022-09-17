package top.bettercode.summer.util.wechat.controller

import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseBody
import top.bettercode.logging.annotation.RequestLogging
import top.bettercode.simpleframework.web.BaseController
import top.bettercode.summer.util.wechat.support.IWechatService
import top.bettercode.summer.util.wechat.support.WechatToken
import top.bettercode.summer.util.wechat.support.offiaccount.IOffiaccountClient
import javax.validation.constraints.NotBlank

@ConditionalOnWebApplication
@Controller
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
        var token: WechatToken? = null
        try {
            val accessToken =
                if (code.isNullOrBlank()) null else offiaccountClient.getWebPageAccessToken(code)
            token = if (accessToken?.isOk == true) wechatService.oauth(
                accessToken,
                (if (offiaccountClient.properties.userUnionid) offiaccountClient.getSnsapiUserinfo(
                    accessToken.accessToken!!,
                    accessToken.openid!!
                ) else null)
            ) else null
            token?.openId = accessToken?.openid
        } catch (e: Exception) {
            log.warn("token获取失败", e)
        }
        return offiaccountClient.properties.redirectUrl(token, wechatService.forceLogin(), state)
    }

    /*
   * js签名
   */
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