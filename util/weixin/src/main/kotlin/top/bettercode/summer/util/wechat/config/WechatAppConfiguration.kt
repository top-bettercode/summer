package top.bettercode.summer.util.wechat.config

import org.springframework.boot.autoconfigure.condition.ConditionalOnBean
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import top.bettercode.summer.util.wechat.controller.MiniprogramCallbackController
import top.bettercode.summer.util.wechat.support.IWechatService
import top.bettercode.summer.util.wechat.support.miniprogram.IMiniprogramClient
import top.bettercode.summer.util.wechat.support.miniprogram.MiniprogramClient

@Configuration(proxyBeanMethods = false)
@ConditionalOnBean(IMiniprogramProperties::class)
class WechatAppConfiguration(private val miniprogramProperties: IMiniprogramProperties) {

    @Bean
    fun miniprogramClient(): IMiniprogramClient {
        return MiniprogramClient(miniprogramProperties)
    }

    @ConditionalOnWebApplication
    @Bean
    fun miniprogramCallbackController(
        wechatService: IWechatService,
        miniprogramClient: IMiniprogramClient
    ): MiniprogramCallbackController {
        return MiniprogramCallbackController(wechatService, miniprogramClient)
    }

}