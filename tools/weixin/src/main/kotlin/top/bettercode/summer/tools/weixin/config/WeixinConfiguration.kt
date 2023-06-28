package top.bettercode.summer.tools.weixin.config

import org.springframework.boot.autoconfigure.condition.ConditionalOnBean
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import top.bettercode.summer.tools.weixin.controller.OffiaccountCallbackController
import top.bettercode.summer.tools.weixin.properties.IOffiaccountProperties
import top.bettercode.summer.tools.weixin.support.IWeixinService
import top.bettercode.summer.tools.weixin.support.offiaccount.IOffiaccountClient
import top.bettercode.summer.tools.weixin.support.offiaccount.OffiaccountClient

@Configuration(proxyBeanMethods = false)
@ConditionalOnBean(IOffiaccountProperties::class)
class WeixinConfiguration(private val offiaccountProperties: IOffiaccountProperties) {

    @Bean
    fun offiaccountClient(): IOffiaccountClient {
        return OffiaccountClient(offiaccountProperties)
    }

    @ConditionalOnBean(IWeixinService::class)
    @ConditionalOnWebApplication
    @Bean
    fun offiaccountCallbackController(
            wechatService: IWeixinService,
            offiaccountClient: IOffiaccountClient
    ): OffiaccountCallbackController {
        return OffiaccountCallbackController(wechatService, offiaccountClient)
    }

}