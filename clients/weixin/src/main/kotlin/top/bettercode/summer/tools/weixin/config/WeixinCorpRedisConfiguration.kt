package top.bettercode.summer.tools.weixin.config

import org.springframework.boot.autoconfigure.condition.ConditionalOnBean
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.data.redis.connection.RedisConnectionFactory
import top.bettercode.summer.tools.weixin.properties.CorpProperties
import top.bettercode.summer.tools.weixin.support.IWeixinService
import top.bettercode.summer.tools.weixin.support.RedisWeixinCache
import top.bettercode.summer.tools.weixin.support.corp.CorpClient

@ConditionalOnClass(RedisConnectionFactory::class)
@Configuration(proxyBeanMethods = false)
@ConditionalOnBean(CorpProperties::class, RedisConnectionFactory::class)
class WeixinCorpRedisConfiguration(private val properties: CorpProperties) {

    @Bean
    fun corpClient(redisConnectionFactory: RedisConnectionFactory): CorpClient {
        return CorpClient(
            properties,
            RedisWeixinCache(properties.cacheSeconds, CorpClient.MARKER, redisConnectionFactory)
        )
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