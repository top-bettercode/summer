package top.bettercode.summer.tools.weixin.config

import org.springframework.boot.autoconfigure.condition.ConditionalOnBean
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import top.bettercode.summer.tools.weixin.controller.MiniprogramCallbackController
import top.bettercode.summer.tools.weixin.properties.IMiniprogramProperties
import top.bettercode.summer.tools.weixin.support.IWechatService
import top.bettercode.summer.tools.weixin.support.miniprogram.IMiniprogramClient
import top.bettercode.summer.tools.weixin.support.miniprogram.MiniprogramClient

@Configuration(proxyBeanMethods = false)
@ConditionalOnBean(IMiniprogramProperties::class)
class WechatAppConfiguration(private val miniprogramProperties: IMiniprogramProperties) {

    @Bean
    fun miniprogramClient(): IMiniprogramClient {
        return MiniprogramClient(miniprogramProperties)
    }

    @ConditionalOnBean(IWechatService::class)
    @ConditionalOnWebApplication
    @Bean
    fun miniprogramCallbackController(
        wechatService: IWechatService,
        miniprogramClient: IMiniprogramClient
    ): MiniprogramCallbackController {
        return MiniprogramCallbackController(wechatService, miniprogramClient)
    }

}