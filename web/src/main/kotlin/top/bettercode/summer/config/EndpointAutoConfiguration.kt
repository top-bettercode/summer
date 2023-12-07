package top.bettercode.summer.config

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.actuate.autoconfigure.endpoint.web.WebEndpointAutoConfiguration
import org.springframework.boot.actuate.autoconfigure.endpoint.web.WebEndpointProperties
import org.springframework.boot.actuate.autoconfigure.web.server.ManagementServerProperties
import org.springframework.boot.autoconfigure.AutoConfigureAfter
import org.springframework.boot.autoconfigure.condition.*
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties
import org.springframework.boot.autoconfigure.web.ServerProperties
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.*
import org.springframework.core.env.Environment
import org.springframework.core.io.ResourceLoader
import org.springframework.core.type.AnnotatedTypeMetadata
import top.bettercode.summer.logging.WebsocketProperties
import top.bettercode.summer.tools.lang.util.IPAddressUtil
import top.bettercode.summer.tools.lang.util.RandomUtil
import top.bettercode.summer.web.properties.CorsProperties
import java.net.InetAddress
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
class EndpointAutoConfiguration(managementServerProperties: ManagementServerProperties) {

    private val log: Logger = LoggerFactory.getLogger(EndpointAutoConfiguration::class.java)


    init {
        val port = managementServerProperties.port
        if (port != null && managementServerProperties.address == null) {
            managementServerProperties.address = InetAddress.getByName(IPAddressUtil.inet4Address)
        }
    }

    @Bean
    fun settingsEndpoint(): SettingsEndpoint {
        return SettingsEndpoint()
    }

    @ConditionalOnProperty(
            prefix = "summer.management.auth",
            name = ["enabled"],
            havingValue = "true"
    )
    @Bean
    @ConditionalOnMissingBean(ManagementLoginPageGeneratingFilter::class)
    fun managementLoginPageGeneratingFilter(
            managementAuthProperties: ManagementAuthProperties,
            webEndpointProperties: WebEndpointProperties
    ): ManagementLoginPageGeneratingFilter {
        if (managementAuthProperties.password.isNullOrBlank()) {
            managementAuthProperties.password = RandomUtil.nextString2(6)
            log.info(
                    "默认日志访问用户名密码：{}:{}", managementAuthProperties.username,
                    managementAuthProperties.password
            )
        }
        return ManagementLoginPageGeneratingFilter(managementAuthProperties, webEndpointProperties)
    }

    @ConditionalOnWebApplication
    @Bean
    fun docsEndpoint(
            @Autowired(required = false) request: HttpServletRequest,
            @Autowired(required = false) response: HttpServletResponse,
            resourceLoader: ResourceLoader,
            corsProperties: CorsProperties
    ): DocsEndpoint {
        return DocsEndpoint(request, response, resourceLoader, corsProperties)
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
            @Value("\${summer.logging.files.view-path:#{'\${summer.logging.files.path}'}}") loggingFilesPath: String,
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


    @ConditionalOnProperty(
            prefix = "summer.gen",
            name = ["enabled"],
            havingValue = "true",
            matchIfMissing = true
    )
    @ConditionalOnWebApplication
    @Bean
    fun genEndpoint(
            @Autowired(required = false) response: HttpServletResponse,
            dataSourceProperties: DataSourceProperties? = null,
            environment: Environment,
            serverProperties: ServerProperties,
            webEndpointProperties: WebEndpointProperties
    ): GenEndpoint {
        return GenEndpoint(response, dataSourceProperties, environment, serverProperties, webEndpointProperties)
    }


    internal class LogsEndpointCondition : Condition {

        override fun matches(context: ConditionContext, metadata: AnnotatedTypeMetadata): Boolean {
            return !context.environment.getProperty("summer.logging.files.path").isNullOrBlank()
        }
    }

}