package top.bettercode.summer.tools.weixin.config

import org.springframework.boot.autoconfigure.condition.ConditionalOnBean
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingClass
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import top.bettercode.summer.tools.weixin.properties.ICorpProperties
import top.bettercode.summer.tools.weixin.support.DefaultWeixinCache
import top.bettercode.summer.tools.weixin.support.IWeixinService
import top.bettercode.summer.tools.weixin.support.corp.CorpClient

@ConditionalOnMissingClass("org.springframework.data.redis.connection.RedisConnectionFactory")
@Configuration(proxyBeanMethods = false)
@ConditionalOnBean(ICorpProperties::class)
class WeixinCorpConfiguration(private val properties: ICorpProperties) {

    @Bean
    fun corpClient(): CorpClient {
        return CorpClient(properties, DefaultWeixinCache(properties.cacheSeconds))
    }

    @ConditionalOnBean(IWeixinService::class)
    @ConditionalOnWebApplication
    @Bean
    fun corpCallbackController(
            wechatService: IWeixinService,
            corpClient: CorpClient
    ): top.bettercode.summer.tools.weixin.controller.CorpCallbackController {
        return top.bettercode.summer.tools.weixin.controller.CorpCallbackController(
                wechatService,
                corpClient
        )
    }

}