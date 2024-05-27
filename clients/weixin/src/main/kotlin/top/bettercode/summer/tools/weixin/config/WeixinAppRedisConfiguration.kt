package top.bettercode.summer.tools.weixin.config

import org.springframework.boot.autoconfigure.condition.ConditionalOnBean
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.data.redis.connection.RedisConnectionFactory
import top.bettercode.summer.tools.weixin.controller.MiniprogramCallbackController
import top.bettercode.summer.tools.weixin.properties.MiniprogramProperties
import top.bettercode.summer.tools.weixin.support.IWeixinService
import top.bettercode.summer.tools.weixin.support.RedisWeixinCache
import top.bettercode.summer.tools.weixin.support.miniprogram.MiniprogramClient

@ConditionalOnClass(RedisConnectionFactory::class)
@Configuration(proxyBeanMethods = false)
@ConditionalOnBean(MiniprogramProperties::class, RedisConnectionFactory::class)
class WeixinAppRedisConfiguration(private val properties: MiniprogramProperties) {

    @Bean
    fun miniprogramClient(redisConnectionFactory: RedisConnectionFactory): MiniprogramClient {
        return MiniprogramClient(
            properties,
            RedisWeixinCache(
                properties.cacheSeconds,
                MiniprogramClient.MARKER,
                redisConnectionFactory
            )
        )
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