package top.bettercode.summer.tools.weixin.config

import org.springframework.boot.autoconfigure.condition.ConditionalOnBean
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import top.bettercode.summer.tools.weixin.controller.OffiaccountCallbackController
import top.bettercode.summer.tools.weixin.properties.IOffiaccountProperties
import top.bettercode.summer.tools.weixin.support.IWechatService
import top.bettercode.summer.tools.weixin.support.offiaccount.IOffiaccountClient
import top.bettercode.summer.tools.weixin.support.offiaccount.OffiaccountClient

@Configuration(proxyBeanMethods = false)
@ConditionalOnBean(IOffiaccountProperties::class)
class WechatConfiguration(private val offiaccountProperties: IOffiaccountProperties) {

    @Bean
    fun offiaccountClient(): IOffiaccountClient {
        return OffiaccountClient(offiaccountProperties)
    }

    @ConditionalOnBean(IWechatService::class)
    @ConditionalOnWebApplication
    @Bean
    fun offiaccountCallbackController(
        wechatService: IWechatService,
        offiaccountClient: IOffiaccountClient
    ): OffiaccountCallbackController {
        return OffiaccountCallbackController(wechatService, offiaccountClient)
    }

}