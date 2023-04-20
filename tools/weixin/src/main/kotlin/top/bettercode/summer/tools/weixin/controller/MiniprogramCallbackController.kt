package top.bettercode.summer.tools.weixin.controller

import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication
import org.springframework.stereotype.Controller
import org.springframework.util.Assert
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseBody
import top.bettercode.summer.logging.annotation.RequestLogging
import top.bettercode.summer.security.authorize.Anonymous
import top.bettercode.summer.tools.weixin.support.IWechatService
import top.bettercode.summer.tools.weixin.support.WechatToken
import top.bettercode.summer.tools.weixin.support.miniprogram.IMiniprogramClient
import top.bettercode.summer.web.BaseController
import javax.validation.constraints.NotBlank

@ConditionalOnWebApplication
@Controller
@Anonymous
@Validated
@RequestMapping(value = ["/wechat"], name = "微信")
class MiniprogramCallbackController(
        private val wechatService: IWechatService,
        private val miniprogramClient: IMiniprogramClient
) : BaseController() {

    @RequestLogging(ignoredTimeout = true)
    @ResponseBody
    @PostMapping(
            value = ["\${summer.wechat.mini.oauth-mapping-path:/miniOauth}"],
            name = "小程序code2Session授权接口"
    )
    fun miniOauth(@NotBlank code: String, encryptedData: String?, iv: String?): Any {
        log.debug("code:{}", code)
        return try {
            val jsSession =
                    miniprogramClient.jscode2session(code).decrypt(encryptedData, iv)
            val result = if (jsSession.isOk)
                try {
                    wechatService.miniOauth(jsSession)
                } catch (e: Exception) {
                    log.warn(e.message, e)
                    WechatToken(e.message)
                } else WechatToken()
            result.openId = jsSession.openid ?: ""
            result.unionId = jsSession.unionid ?: ""
            result.hasBound = result.accessToken.isBlank().not()
            ok(result)
        } catch (e: Exception) {
            log.error("授权失败", e)
            val result = WechatToken(e.message)
            result.accessToken = ""
            result.openId = ""
            result.hasBound = false
            ok(result)
        }
    }


    @RequestLogging(ignoredTimeout = true)
    @ResponseBody
    @PostMapping(value = ["/miniPhoneOauth"], name = "小程序手机号授权接口")
    fun miniPhoneOauth(@NotBlank code: String): Any {
        log.debug("code:{}", code)
        val phoneInfoResp = miniprogramClient.getuserphonenumber(code)
        Assert.isTrue(phoneInfoResp.isOk, phoneInfoResp.errmsg ?: "手机号授权失败")
        val phoneInfo = if (phoneInfoResp.isOk) phoneInfoResp.phoneInfo else null
        Assert.notNull(phoneInfo, "获取手机号失败")
        log.info("phoneInfo:{}", phoneInfo)
        val result = wechatService.phoneOauth(phoneInfo!!)
        result.hasBound = result.accessToken.isBlank().not()
        return ok(result)
    }

}