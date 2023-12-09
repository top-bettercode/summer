package top.bettercode.summer.config

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.actuate.autoconfigure.endpoint.web.WebEndpointAutoConfiguration
import org.springframework.boot.actuate.autoconfigure.endpoint.web.WebEndpointProperties
import org.springframework.boot.actuate.autoconfigure.web.server.ManagementServerProperties
import org.springframework.boot.autoconfigure.AutoConfigureAfter
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties
import org.springframework.boot.autoconfigure.web.ServerProperties
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.*
import org.springframework.core.env.Environment
import org.springframework.core.io.ResourceLoader
import org.springframework.core.type.AnnotatedTypeMetadata
import top.bettercode.summer.logging.WebsocketProperties
import top.bettercode.summer.tools.lang.util.IPAddressUtil
import top.bettercode.summer.web.properties.CorsProperties
import java.net.InetAddress
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

/**
 * @author Peter Wu
 */
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
@ConditionalOnClass(WebEndpointProperties::class)
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
            log.info("management server address:{}:{}", managementServerProperties.address, port)
        }
    }

    @Bean
    fun settingsEndpoint(): SettingsEndpoint {
        return SettingsEndpoint()
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
    ): PumlEndpoint {
        return PumlEndpoint(response, dataSourceProperties, environment, serverProperties, webEndpointProperties)
    }


    internal class LogsEndpointCondition : Condition {

        override fun matches(context: ConditionContext, metadata: AnnotatedTypeMetadata): Boolean {
            return !context.environment.getProperty("summer.logging.files.path").isNullOrBlank()
        }
    }

}