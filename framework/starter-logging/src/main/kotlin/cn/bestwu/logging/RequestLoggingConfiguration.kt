package cn.bestwu.logging

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.boot.web.servlet.support.ErrorPageFilter
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.Ordered
import org.springframework.core.annotation.Order
import org.springframework.web.servlet.config.annotation.InterceptorRegistry
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer

/**
 * 自动增加请求日志过滤器
 *
 * @author Peter Wu
 * @since 0.1.5
 */
@ConditionalOnProperty(prefix = "logging.request", name = ["enabled"], havingValue = "true", matchIfMissing = true)
@Configuration
@EnableConfigurationProperties(RequestLoggingProperties::class)
class RequestLoggingConfiguration {
    @Order(Ordered.HIGHEST_PRECEDENCE)
    @Configuration
    class CustomWebMvcConfigurer : WebMvcConfigurer {
        @Autowired
        private lateinit var properties: RequestLoggingProperties

        override fun addInterceptors(registry: InterceptorRegistry) {
            registry.addInterceptor(HandlerMethodHandlerInterceptor(properties))
        }
    }

    @Bean
    fun requestLoggingFilter(properties: RequestLoggingProperties, handlers: List<RequestLoggingHandler>?): RequestLoggingFilter {
        return RequestLoggingFilter(properties, handlers ?: emptyList())
    }


    @ConditionalOnProperty(prefix = "logging.show", name = ["enabled"], havingValue = "true", matchIfMissing = true)
    @Bean
    fun logsController(@Value("\${logging.files.path}") loggingFilesPath: String): LogsController {
        return LogsController(loggingFilesPath)
    }

    @Bean
    @Order(Ordered.HIGHEST_PRECEDENCE + 1)
    @ConditionalOnMissingBean(ErrorPageFilter::class)
    fun errorPageFilter(): ErrorPageFilter {
        return ErrorPageFilter()
    }
}
