package top.bettercode.summer.logging

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.boot.web.servlet.support.ErrorPageFilter
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.Ordered
import org.springframework.core.annotation.Order

/**
 * 自动增加请求日志过滤器
 *
 * @author Peter Wu
 * @since 0.1.5
 */
@ConditionalOnProperty(
        prefix = "summer.logging.request",
        name = ["enabled"],
        havingValue = "true",
        matchIfMissing = true
)
@Configuration(proxyBeanMethods = false)
@EnableConfigurationProperties(
        RequestLoggingProperties::class,
        WebsocketProperties::class
)
@ConditionalOnClass(javax.servlet.Filter::class)
class RequestLoggingConfiguration {

    @Bean
    fun requestLoggingFilter(
            properties: RequestLoggingProperties,
            @Autowired(required = false) handlers: List<RequestLoggingHandler>?
    ): RequestLoggingFilter {
        return RequestLoggingFilter(properties, handlers ?: emptyList())
    }

    @Bean
    fun requestContentReadFilter(): RequestContentReadFilter {
        return RequestContentReadFilter()
    }

    @Bean
    @Order(Ordered.HIGHEST_PRECEDENCE + 1)
    @ConditionalOnMissingBean(ErrorPageFilter::class)
    fun errorPageFilter(): ErrorPageFilter {
        return ErrorPageFilter()
    }
}
