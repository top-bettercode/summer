package top.bettercode.summer.util.wechat.config

import org.springframework.boot.autoconfigure.condition.ConditionalOnBean
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import top.bettercode.summer.util.wechat.controller.CorpCallbackController
import top.bettercode.summer.util.wechat.support.IWechatService
import top.bettercode.summer.util.wechat.support.corp.CorpClient
import top.bettercode.summer.util.wechat.support.corp.ICorpClient

@Configuration(proxyBeanMethods = false)
@ConditionalOnBean(ICorpProperties::class)
class WechatCorpConfiguration(private val corpProperties: ICorpProperties) {

    @Bean
    fun corpClient(): ICorpClient {
        return CorpClient(corpProperties)
    }

    @ConditionalOnWebApplication
    @Bean
    fun corpCallbackController(
        wechatService: IWechatService,
        corpClient: ICorpClient
    ): CorpCallbackController {
        return CorpCallbackController(wechatService, corpClient)
    }

}