package top.bettercode.summer.config

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.actuate.autoconfigure.endpoint.web.WebEndpointAutoConfiguration
import org.springframework.boot.actuate.autoconfigure.endpoint.web.WebEndpointProperties
import org.springframework.boot.autoconfigure.AutoConfigureAfter
import org.springframework.boot.autoconfigure.condition.*
import org.springframework.boot.autoconfigure.web.ServerProperties
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.*
import org.springframework.core.env.Environment
import org.springframework.core.io.ResourceLoader
import org.springframework.core.type.AnnotatedTypeMetadata
import org.springframework.util.StringUtils
import top.bettercode.summer.logging.WebsocketProperties
import top.bettercode.summer.tools.lang.util.RandomUtil
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

/**
 * @author Peter Wu
 */
@ConditionalOnClass(WebEndpointProperties::class, javax.servlet.Filter::class)
@ConditionalOnBean(WebEndpointProperties::class)
@AutoConfigureAfter(WebEndpointAutoConfiguration::class)
@EnableConfigurationProperties(
        ManagementAuthProperties::class
)
@Configuration(proxyBeanMethods = false)
class EndpointAutoConfiguration {

    private val log: Logger = LoggerFactory.getLogger(EndpointAutoConfiguration::class.java)

    @Bean
    fun settingsEndpoint(): SettingsEndpoint {
        return SettingsEndpoint()
    }


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

    @Profile("!default & !dev & !test")
    @Bean
    @ConditionalOnMissingBean(ManagementLoginPageGeneratingFilter::class)
    fun managementLoginPageGeneratingFilter(
            managementAuthProperties: ManagementAuthProperties,
            webEndpointProperties: WebEndpointProperties
    ): ManagementLoginPageGeneratingFilter {
        if (!StringUtils.hasText(managementAuthProperties.password)) {
            managementAuthProperties.password = RandomUtil.nextString2(6)
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
            @Autowired(required = false) response: HttpServletResponse,
            webEndpointProperties: WebEndpointProperties

    ): LogsEndpoint {
        return LogsEndpoint(
                loggingFilesPath,
                environment,
                websocketProperties,
                serverProperties,
                response,
                webEndpointProperties
        )
    }

    internal class LogsEndpointCondition : Condition {

        override fun matches(context: ConditionContext, metadata: AnnotatedTypeMetadata): Boolean {
            return StringUtils.hasText(context.environment.getProperty("summer.logging.files.path"))
        }
    }

}