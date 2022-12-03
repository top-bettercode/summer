package top.bettercode.summer.tools.weixin.controller

import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import top.bettercode.summer.logging.annotation.RequestLogging
import top.bettercode.summer.tools.weixin.support.IWechatService
import top.bettercode.summer.tools.weixin.support.WechatToken
import top.bettercode.summer.tools.weixin.support.corp.ICorpClient
import top.bettercode.summer.web.BaseController

@ConditionalOnWebApplication
@Controller
@RequestMapping(value = ["/wechat"], name = "微信")
class CorpCallbackController(
    private val wechatService: IWechatService,
    private val corpClient: ICorpClient
) : BaseController() {

    /*
   * 企业号OAuth回调接口
   */
    @RequestLogging(ignoredTimeout = true)
    @GetMapping(value = ["/corpOauth"], name = "企业号OAuth回调接口")
    fun oauth(code: String?, state: String?): String {
        plainTextError()
        var token: WechatToken? = null
        try {
            val accessToken =
                if (code.isNullOrBlank()) null else corpClient.getWebPageAccessToken(code)
            token = if (accessToken?.isOk == true) wechatService.corpOauth(accessToken) else null
            token?.openId = accessToken?.openid
        } catch (e: Exception) {
            log.warn("token获取失败", e)
        }
        return corpClient.properties.redirectUrl(token, wechatService.forceLogin(), state)
    }


}