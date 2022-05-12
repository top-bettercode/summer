package top.bettercode.summer.util.wechat.config

import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

/**
 * @author Peter Wu
 */
@Configuration(proxyBeanMethods = false)
class PropertiesConfiguration {

    @ConditionalOnProperty(prefix = "summer.wechat.mini", name = ["app-id"])
    @ConfigurationProperties(prefix = "summer.wechat.mini")
    @ConditionalOnMissingBean
    @Bean
    fun miniprogramProperties(): IMiniprogramProperties {
        return MiniprogramProperties()
    }

    @ConditionalOnProperty(prefix = "summer.wechat", name = ["app-id"])
    @ConfigurationProperties(prefix = "summer.wechat")
    @ConditionalOnMissingBean
    @Bean
    fun offiaccountProperties(): IOffiaccountProperties {
        return OffiaccountProperties()
    }
}