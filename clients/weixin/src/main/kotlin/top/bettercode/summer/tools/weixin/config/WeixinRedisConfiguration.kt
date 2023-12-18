package top.bettercode.summer.tools.weixin.config

import org.springframework.boot.autoconfigure.condition.ConditionalOnBean
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.data.redis.connection.RedisConnectionFactory
import top.bettercode.summer.tools.weixin.controller.OffiaccountCallbackController
import top.bettercode.summer.tools.weixin.properties.IOffiaccountProperties
import top.bettercode.summer.tools.weixin.support.DefaultDuplicatedMessageChecker
import top.bettercode.summer.tools.weixin.support.IDuplicatedMessageChecker
import top.bettercode.summer.tools.weixin.support.IWeixinService
import top.bettercode.summer.tools.weixin.support.RedisWeixinCache
import top.bettercode.summer.tools.weixin.support.offiaccount.OffiaccountClient

@ConditionalOnClass(RedisConnectionFactory::class)
@Configuration(proxyBeanMethods = false)
@ConditionalOnBean(IOffiaccountProperties::class, RedisConnectionFactory::class)
class WeixinRedisConfiguration(private val properties: IOffiaccountProperties) {

    @Bean
    fun offiaccountClient(redisConnectionFactory: RedisConnectionFactory): OffiaccountClient {
        return OffiaccountClient(properties, RedisWeixinCache(properties.cacheSeconds, OffiaccountClient.LOG_MARKER, redisConnectionFactory))
    }

    @ConditionalOnBean(IWeixinService::class)
    @ConditionalOnWebApplication
    @Bean
    fun offiaccountCallbackController(
            wechatService: IWeixinService,
            offiaccountClient: OffiaccountClient,
            duplicatedMessageChecker: IDuplicatedMessageChecker
    ): OffiaccountCallbackController {
        return OffiaccountCallbackController(wechatService, offiaccountClient, duplicatedMessageChecker)
    }

    @ConditionalOnMissingBean(IDuplicatedMessageChecker::class)
    @ConditionalOnWebApplication
    @Bean
    fun duplicatedMessageChecker(): IDuplicatedMessageChecker {
        return DefaultDuplicatedMessageChecker()
    }
}