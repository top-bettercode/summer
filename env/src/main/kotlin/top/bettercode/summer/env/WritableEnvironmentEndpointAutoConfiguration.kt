package top.bettercode.summer.env

import org.springframework.boot.actuate.autoconfigure.endpoint.condition.ConditionalOnAvailableEndpoint
import org.springframework.boot.actuate.autoconfigure.endpoint.web.WebEndpointProperties
import org.springframework.boot.actuate.autoconfigure.env.EnvironmentEndpointAutoConfiguration
import org.springframework.boot.actuate.autoconfigure.env.EnvironmentEndpointProperties
import org.springframework.boot.actuate.env.EnvironmentEndpoint
import org.springframework.boot.autoconfigure.AutoConfigureAfter
import org.springframework.boot.autoconfigure.AutoConfigureBefore
import org.springframework.boot.autoconfigure.condition.*
import org.springframework.boot.autoconfigure.web.servlet.WebMvcAutoConfiguration
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.env.ConfigurableEnvironment
import org.springframework.core.env.Environment

/**
 * [Auto-configuration][EnableAutoConfiguration] for the [WritableEnvironmentEndpoint].
 */
@Configuration(proxyBeanMethods = false)
@ConditionalOnMissingClass("org.springframework.cloud.autoconfigure.WritableEnvironmentEndpointAutoConfiguration")
@ConditionalOnBean(WebEndpointProperties::class)
@ConditionalOnClass(EnvironmentEndpoint::class, EnvironmentEndpointProperties::class)
@AutoConfigureBefore(EnvironmentEndpointAutoConfiguration::class)
@AutoConfigureAfter(WebMvcAutoConfiguration::class)
@EnableConfigurationProperties(EnvironmentEndpointProperties::class)
class WritableEnvironmentEndpointAutoConfiguration(private val properties: EnvironmentEndpointProperties) {
    @Bean
    @ConditionalOnMissingBean(search = SearchStrategy.CURRENT)
    fun configurationPropertiesRebinder(
            beans: ConfigurationPropertiesBeans
    ): ConfigurationPropertiesRebinder {
        return ConfigurationPropertiesRebinder(beans)
    }

    @Bean
    @ConditionalOnMissingBean
    fun environmentManager(environment: ConfigurableEnvironment): EnvironmentManager {
        return EnvironmentManager(environment)
    }

    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnAvailableEndpoint
    fun writableEnvironmentEndpoint(environment: Environment): WritableEnvironmentEndpoint {
        val endpoint = WritableEnvironmentEndpoint(environment)
        val keysToSanitize = properties.keysToSanitize
        if (keysToSanitize != null) {
            endpoint.setKeysToSanitize(*keysToSanitize)
        }
        return endpoint
    }

    @Bean
    @ConditionalOnAvailableEndpoint
    fun writableEnvironmentEndpointWebExtension(
            endpoint: WritableEnvironmentEndpoint, environment: EnvironmentManager
    ): WritableEnvironmentEndpointWebExtension {
        return WritableEnvironmentEndpointWebExtension(endpoint, environment)
    }

    @Bean
    @ConditionalOnAvailableEndpoint
    fun configEndpoint(environment: EnvironmentManager): ConfigEndpoint {
        return ConfigEndpoint(environment)
    }

    companion object {
        @Bean
        @ConditionalOnMissingBean(search = SearchStrategy.CURRENT)
        fun configurationPropertiesBeans(): ConfigurationPropertiesBeans {
            return ConfigurationPropertiesBeans()
        }
    }
}
