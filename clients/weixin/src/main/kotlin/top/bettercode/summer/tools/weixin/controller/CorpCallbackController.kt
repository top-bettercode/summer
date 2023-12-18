package top.bettercode.summer.tools.weixin.controller

import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import top.bettercode.summer.logging.annotation.RequestLogging
import top.bettercode.summer.security.authorize.Anonymous
import top.bettercode.summer.tools.weixin.support.IWeixinService
import top.bettercode.summer.tools.weixin.support.WeixinToken
import top.bettercode.summer.tools.weixin.support.corp.CorpClient
import top.bettercode.summer.web.BaseController

@ConditionalOnWebApplication
@Controller
@Anonymous
@RequestMapping(value = ["/wechat"], name = "微信")
class CorpCallbackController(
        private val wechatService: IWeixinService,
        private val corpClient: CorpClient
) : BaseController() {

    /*
   * 企业号OAuth回调接口
   */
    @RequestLogging(ignoredTimeout = true)
    @GetMapping(value = ["/corpOauth"], name = "企业号OAuth回调接口")
    fun oauth(code: String?, state: String?): String {
        plainTextError()
        val token: WeixinToken =
                try {
                    val accessToken =
                            if (code.isNullOrBlank()) null else corpClient.getWebPageAccessToken(code)
                    val token = if (accessToken?.isOk == true) try {
                        wechatService.corpOauth(accessToken)
                    } catch (e: Exception) {
                        log.warn(e.message, e)
                        WeixinToken(e.message)
                    } else WeixinToken(accessToken?.errmsg)
                    token.openId = accessToken?.openid ?: ""
                    token
                } catch (e: Exception) {
                    log.warn("token获取失败", e)
                    WeixinToken(e.message)
                }
        return corpClient.properties.redirectUrl(token, wechatService.forceLogin(), state)
    }


}