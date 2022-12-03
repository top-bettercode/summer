package top.bettercode.summer.tools.weixin.config

import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

/**
 * @author Peter Wu
 */
@Configuration(proxyBeanMethods = false)
class PropertiesConfiguration {

    @ConditionalOnProperty(prefix = "summer.wechat.corp", name = ["app-id"])
    @ConditionalOnMissingBean
    @Bean
    fun corpProperties(): ICorpProperties {
        return CorpProperties()
    }

    @ConditionalOnProperty(prefix = "summer.wechat.mini", name = ["app-id"])
    @ConditionalOnMissingBean
    @Bean
    fun miniprogramProperties(): IMiniprogramProperties {
        return MiniprogramProperties()
    }

    @ConditionalOnProperty(prefix = "summer.wechat", name = ["app-id"])
    @ConditionalOnMissingBean
    @Bean
    fun offiaccountProperties(): IOffiaccountProperties {
        return OffiaccountProperties()
    }


}