package top.bettercode.summer.tools.weixin.config

import org.springframework.boot.autoconfigure.condition.ConditionalOnBean
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import top.bettercode.summer.tools.weixin.controller.MiniprogramCallbackController
import top.bettercode.summer.tools.weixin.properties.IMiniprogramProperties
import top.bettercode.summer.tools.weixin.support.IWeixinService
import top.bettercode.summer.tools.weixin.support.miniprogram.MiniprogramClient

@Configuration(proxyBeanMethods = false)
@ConditionalOnBean(IMiniprogramProperties::class)
class WeixinAppConfiguration(private val miniprogramProperties: IMiniprogramProperties) {

    @Bean
    fun miniprogramClient(): MiniprogramClient {
        return MiniprogramClient(miniprogramProperties)
    }

    @ConditionalOnBean(IWeixinService::class)
    @ConditionalOnWebApplication
    @Bean
    fun miniprogramCallbackController(
            wechatService: IWeixinService,
            miniprogramClient: MiniprogramClient
    ): MiniprogramCallbackController {
        return MiniprogramCallbackController(wechatService, miniprogramClient)
    }

}