package top.bettercode.summer.util.wechat.controller

import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication
import org.springframework.stereotype.Controller
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseBody
import top.bettercode.logging.annotation.RequestLogging
import top.bettercode.simpleframework.web.BaseController
import top.bettercode.summer.util.wechat.config.WexinProperties
import top.bettercode.summer.util.wechat.support.IWechatService
import top.bettercode.summer.util.wechat.support.miniprogram.MiniprogramClient
import javax.validation.constraints.NotBlank

@ConditionalOnWebApplication
@Controller
@Validated
@RequestMapping(value = ["/wechat"], name = "微信")
class MiniprogramCallbackController(
    private val wechatService: IWechatService,
    private val miniprogramClient: MiniprogramClient
) : BaseController() {

    @RequestLogging(ignoredTimeout = true)
    @ResponseBody
    @PostMapping(value = ["/miniOauth"], name = "小程序code2Session授权接口")
    fun miniOauth(@NotBlank code: String): Any {
        log.debug("code:{}", code)
        return try {
            val jsSession = miniprogramClient.jscode2session(code)
            val openId = if (jsSession.isOk) jsSession.openid else null
            log.info("openId:{}", openId)
            val token = if (openId == null) null else wechatService.oauth(openId)
            val result: MutableMap<String, Any?> = HashMap()
            result["access_token"] = token
            result[WexinProperties.OPEN_ID_NAME] = openId
            result["hasBound"] = token != null
            ok(result)
        } catch (e: Exception) {
            log.error("授权失败", e)
            val result: MutableMap<String, Any?> = HashMap()
            result["access_token"] = null
            result[WexinProperties.OPEN_ID_NAME] = null
            result["hasBound"] = false
            ok(result)
        }
    }


    @RequestLogging(ignoredTimeout = true)
    @ResponseBody
    @PostMapping(value = ["/miniPhoneOauth"], name = "小程序手机号授权接口")
    fun miniPhoneOauth(@NotBlank code: String): Any {
        log.debug("code:{}", code)
        return try {
            val phoneInfoResp = miniprogramClient.getuserphonenumber(code)
            val phoneInfo = if (phoneInfoResp.isOk) phoneInfoResp.phoneInfo else null
            log.info("openId:{}", phoneInfo)
            val token = if (phoneInfo == null) null else wechatService.phoneOauth(phoneInfo)
            val result: MutableMap<String, Any?> = HashMap()
            result["access_token"] = token
            result["message"] = if (token != null) "授权成功" else "授权失败:手机号未绑定"
            ok(result)
        } catch (e: Exception) {
            log.error("授权失败", e)
            val result: MutableMap<String, Any?> = HashMap()
            result["access_token"] = null
            result["message"] = "授权失败:${e.message}"
            ok(result)
        }
    }

}