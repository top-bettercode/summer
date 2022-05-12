package top.bettercode.summer.util.wechat.config

import org.springframework.boot.autoconfigure.condition.ConditionalOnBean
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import top.bettercode.summer.util.wechat.controller.OffiaccountCallbackController
import top.bettercode.summer.util.wechat.support.IWechatService
import top.bettercode.summer.util.wechat.support.offiaccount.OffiaccountClient

@Configuration(proxyBeanMethods = false)
@ConditionalOnBean(IOffiaccountProperties::class)
class WechatConfiguration(private val offiaccountProperties: IOffiaccountProperties) {

    @Bean
    fun offiaccountClient(): OffiaccountClient {
        return OffiaccountClient(offiaccountProperties)
    }

    @Bean
    fun offiaccountCallbackController(
        wechatService: IWechatService,
        offiaccountClient: OffiaccountClient
    ): OffiaccountCallbackController {
        return OffiaccountCallbackController(wechatService, offiaccountClient)
    }

}