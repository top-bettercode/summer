package top.bettercode.summer.tools.weixin.test

import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.context.annotation.Bean
import top.bettercode.summer.tools.weixin.support.IWeixinService
import top.bettercode.summer.tools.weixin.support.WeixinToken
import top.bettercode.summer.tools.weixin.support.corp.entity.CorpWebPageAccessToken
import top.bettercode.summer.tools.weixin.support.miniprogram.entity.JsSession
import top.bettercode.summer.tools.weixin.support.miniprogram.entity.PhoneInfo
import top.bettercode.summer.tools.weixin.support.offiaccount.entity.SnsapiUserinfo
import top.bettercode.summer.tools.weixin.support.offiaccount.entity.WebPageAccessToken

/**
 * @author Peter Wu
 */
@SpringBootApplication
class TestApplication {

    @Bean
    fun wechatService(): IWeixinService {
        return object : IWeixinService {
            override fun phoneOauth(phoneInfo: PhoneInfo): WeixinToken {
                return WeixinToken()
            }

            override fun miniOauth(jsSession: JsSession): WeixinToken {
                return WeixinToken()
            }

            override fun oauth(webPageAccessToken: WebPageAccessToken, snsapiUserinfo: SnsapiUserinfo?): WeixinToken {
                return WeixinToken()
            }

            override fun corpOauth(corpWebPageAccessToken: CorpWebPageAccessToken): WeixinToken {
                return WeixinToken()
            }
        }
    }

    companion object {
        @JvmStatic
        fun main(args: Array<String>) {
            SpringApplication.run(TestApplication::class.java, *args)
        }
    }
}