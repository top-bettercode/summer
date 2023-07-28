package top.bettercode.summer.tools.weixin.config

import org.springframework.boot.autoconfigure.condition.ConditionalOnBean
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import top.bettercode.summer.tools.weixin.properties.ICorpProperties
import top.bettercode.summer.tools.weixin.support.IWeixinService
import top.bettercode.summer.tools.weixin.support.corp.CorpClient
import top.bettercode.summer.tools.weixin.support.corp.ICorpClient

@Configuration(proxyBeanMethods = false)
@ConditionalOnBean(ICorpProperties::class)
class WeixinCorpConfiguration(private val corpProperties: ICorpProperties) {

    @Bean
    fun corpClient(): ICorpClient {
        return CorpClient(corpProperties)
    }

    @ConditionalOnBean(IWeixinService::class)
    @ConditionalOnWebApplication
    @Bean
    fun corpCallbackController(
            wechatService: IWeixinService,
            corpClient: ICorpClient
    ): top.bettercode.summer.tools.weixin.controller.CorpCallbackController {
        return top.bettercode.summer.tools.weixin.controller.CorpCallbackController(
                wechatService,
                corpClient
        )
    }

}