package top.bettercode.summer.util.wechat.config

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import top.bettercode.summer.util.wechat.controller.MiniprogramCallbackController
import top.bettercode.summer.util.wechat.support.IWechatService
import top.bettercode.summer.util.wechat.support.miniprogram.MiniprogramClient

@ConditionalOnProperty(prefix = "summer.wechat.mini", name = ["app-id"])
@Configuration(proxyBeanMethods = false)
@EnableConfigurationProperties(
    MiniprogramProperties::class
)
class WechatAppConfiguration(private val miniprogramProperties: MiniprogramProperties) {

    @Bean
    fun miniprogramClient(): MiniprogramClient {
        return MiniprogramClient(miniprogramProperties)
    }

    @Bean
    fun miniprogramCallbackController(
        wechatService: IWechatService,
        miniprogramClient: MiniprogramClient
    ): MiniprogramCallbackController {
        return MiniprogramCallbackController(wechatService, miniprogramClient)
    }

}