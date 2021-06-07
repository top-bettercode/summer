package top.bettercode.logging

import top.bettercode.lang.util.RandomUtil.nextString2
import top.bettercode.simpleframework.web.filter.top.bettercode.simpleframework.web.filter.ManagementLoginPageGeneratingFilter
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.actuate.autoconfigure.endpoint.web.WebEndpointProperties
import org.springframework.boot.autoconfigure.AutoConfigureAfter
import org.springframework.boot.autoconfigure.condition.*
import org.springframework.boot.autoconfigure.web.ServerProperties
import org.springframework.context.annotation.*
import org.springframework.core.env.Environment
import org.springframework.core.io.ResourceLoader
import org.springframework.core.type.AnnotatedTypeMetadata
import org.springframework.util.StringUtils
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

/**
 * 自动增加请求日志过滤器
 *
 * @author Peter Wu
 * @since 0.1.5
 */
@ConditionalOnBean(WebEndpointProperties::class)
@ConditionalOnClass(WebEndpointProperties::class)
@AutoConfigureAfter(org.springframework.boot.actuate.autoconfigure.endpoint.web.WebEndpointAutoConfiguration::class)
@Configuration(proxyBeanMethods = false)
class ActuateLoggingConfiguration {

    private val log: Logger = LoggerFactory.getLogger(ActuateLoggingConfiguration::class.java)


    @Bean
    fun navFilter(
        webEndpointProperties: WebEndpointProperties,
        resourceLoader: ResourceLoader
    ): NavFilter {
        return NavFilter(
            webEndpointProperties,
            resourceLoader
        )
    }

    @Profile("release")
    @Bean
    @ConditionalOnMissingBean(ManagementLoginPageGeneratingFilter::class)
    fun managementLoginPageGeneratingFilter(
        managementAuthProperties: ManagementAuthProperties,
        webEndpointProperties: WebEndpointProperties
    ): ManagementLoginPageGeneratingFilter {
        if (!StringUtils.hasText(managementAuthProperties.password)) {
            managementAuthProperties.password = nextString2(6)
            log.info(
                "默认日志访问用户名密码：{}:{}", managementAuthProperties.username,
                managementAuthProperties.password
            )
        }
        return ManagementLoginPageGeneratingFilter(managementAuthProperties, webEndpointProperties)
    }

    @ConditionalOnProperty(
        prefix = "summer.logging",
        name = ["show-enabled"],
        havingValue = "true",
        matchIfMissing = true
    )
    @Conditional(LogsEndpointCondition::class)
    @ConditionalOnWebApplication
    @Bean
    fun logsEndpoint(
        @Value("\${summer.logging.files.path}") loggingFilesPath: String,
        environment: Environment,
        websocketProperties: WebsocketProperties,
        serverProperties: ServerProperties,
        @Autowired(required = false) request: HttpServletRequest,
        @Autowired(required = false) response: HttpServletResponse
    ): LogsEndpoint {
        return LogsEndpoint(
            loggingFilesPath,
            environment,
            websocketProperties,
            serverProperties,
            request,
            response
        )
    }

    internal class LogsEndpointCondition : Condition {

        override fun matches(context: ConditionContext, metadata: AnnotatedTypeMetadata): Boolean {
            return StringUtils.hasText(context.environment.getProperty("summer.logging.files.path"))
        }
    }

}
