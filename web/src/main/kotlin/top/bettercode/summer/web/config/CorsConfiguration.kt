package top.bettercode.summer.web.config

import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.Ordered
import org.springframework.core.annotation.Order
import org.springframework.web.cors.CorsConfigurationSource
import org.springframework.web.cors.UrlBasedCorsConfigurationSource
import org.springframework.web.filter.CorsFilter
import top.bettercode.summer.web.properties.CorsProperties

@ConditionalOnClass(javax.servlet.Filter::class)
@ConditionalOnProperty(prefix = "summer.security.cors", value = ["enable"], havingValue = "true", matchIfMissing = true)
@Configuration(proxyBeanMethods = false)
@ConditionalOnMissingBean(CorsFilter::class)
@EnableConfigurationProperties(CorsProperties::class)
class CorsConfiguration {
    @Bean("corsConfigurationSource")
    fun corsConfigurationSource(corsProperties: CorsProperties): CorsConfigurationSource {
        val source = UrlBasedCorsConfigurationSource()
        source.registerCorsConfiguration(corsProperties.path, corsProperties)
        return source
    }

    @Order(Ordered.HIGHEST_PRECEDENCE)
    @Bean
    fun corsFilter(
            @Qualifier("corsConfigurationSource") configurationSource: CorsConfigurationSource): CorsFilter {
        return CorsFilter(configurationSource)
    }
}
