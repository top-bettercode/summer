package cn.bestwu.logging

import cn.bestwu.lang.util.RandomUtil.nextString2
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.boot.web.servlet.support.ErrorPageFilter
import org.springframework.context.annotation.*
import org.springframework.core.Ordered
import org.springframework.core.annotation.Order
import org.springframework.core.env.Environment
import org.springframework.core.type.AnnotatedTypeMetadata
import org.springframework.util.StringUtils
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
@EnableConfigurationProperties(RequestLoggingProperties::class, WebsocketProperties::class)
class RequestLoggingConfiguration {

    private val log: Logger = LoggerFactory.getLogger(RequestLoggingConfiguration::class.java)

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

    @Profile("release")
    @Bean
    @ConditionalOnMissingBean(LogLoginPageGeneratingFilter::class)
    fun logLoginPageGeneratingFilter(
            logDocAuthProperties: LogDocAuthProperties, @Value("\${logging.view.path:/logs}") logViewPath: String): LogLoginPageGeneratingFilter {
        if (!StringUtils.hasText(logDocAuthProperties.password)) {
            logDocAuthProperties.password = nextString2(6)
            log.info("默认日志访问用户名密码：{}:{}", logDocAuthProperties.username,
                    logDocAuthProperties.password)
        }
        return LogLoginPageGeneratingFilter(logDocAuthProperties, logViewPath)
    }

    @ConditionalOnProperty(prefix = "logging.show", name = ["enabled"], havingValue = "true", matchIfMissing = true)
    @Conditional(LogsControllerCondition::class)
    @Bean
    fun logsController(@Value("\${logging.files.path}") loggingFilesPath: String, environment: Environment, websocketProperties: WebsocketProperties): LogsController {
        return LogsController(loggingFilesPath, environment, websocketProperties)
    }

    internal class LogsControllerCondition : Condition {

        override fun matches(context: ConditionContext, metadata: AnnotatedTypeMetadata): Boolean {
            return StringUtils.hasText(context.environment.getProperty("logging.files.path"))
        }
    }

    @Bean
    @Order(Ordered.HIGHEST_PRECEDENCE + 1)
    @ConditionalOnMissingBean(ErrorPageFilter::class)
    fun errorPageFilter(): ErrorPageFilter {
        return ErrorPageFilter()
    }
}
