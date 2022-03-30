package top.bettercode.autodoc.gen

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.autoconfigure.ImportAutoConfiguration
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.boot.context.properties.bind.Bindable
import org.springframework.boot.context.properties.bind.Binder
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.env.Environment
import top.bettercode.api.sign.ApiSignProperties
import top.bettercode.generator.GeneratorExtension.Companion.defaultModuleName
import top.bettercode.generator.JDBCConnectionConfiguration
import top.bettercode.logging.RequestLoggingConfiguration
import top.bettercode.logging.RequestLoggingProperties
import top.bettercode.simpleframework.config.SummerWebProperties
import javax.annotation.PostConstruct

/**
 * @author Peter Wu
 */
@ConditionalOnProperty(prefix = "summer.autodoc.gen", name = ["enable"], havingValue = "true")
@EnableConfigurationProperties(
    GenProperties::class,
    ApiSignProperties::class,
    SummerWebProperties::class
)
@Configuration(proxyBeanMethods = false)
@ImportAutoConfiguration(RequestLoggingConfiguration::class)
class AutodocConfiguration {
    private val log: Logger = LoggerFactory.getLogger(AutodocConfiguration::class.java)

    @Autowired
    private lateinit var genProperties: GenProperties

    @Autowired
    private lateinit var requestLoggingProperties: RequestLoggingProperties

    @Autowired(required = false)
    private var dataSourceProperties: DataSourceProperties? = null

    @Autowired
    private lateinit var environment: Environment

    @PostConstruct
    fun init() {
        requestLoggingProperties.isFormat = true
        requestLoggingProperties.isForceRecord = true
        requestLoggingProperties.isIncludeRequestBody = true
        requestLoggingProperties.isIncludeResponseBody = true
        requestLoggingProperties.isIncludeTrace = true
    }

    @Bean
    fun autodocHandler(
        signProperties: ApiSignProperties,
        summerWebProperties: SummerWebProperties
    ): AutodocHandler {
        var datasources: Map<String, JDBCConnectionConfiguration>? = null
        try {
            if (dataSourceProperties != null) {
                val configuration = JDBCConnectionConfiguration()
                configuration.url = dataSourceProperties!!.determineUrl() ?: ""
                configuration.username = dataSourceProperties!!.determineUsername() ?: ""
                configuration.password = dataSourceProperties!!.determinePassword() ?: ""
                configuration.driverClass =
                    dataSourceProperties!!.determineDriverClassName() ?: ""
                datasources = mapOf(defaultModuleName to configuration)
            }
        } catch (_: Exception) {
        }
        return AutodocHandler(
            datasources ?: Binder.get(
                environment
            ).bind<Map<String, JDBCConnectionConfiguration>>(
                "summer.datasource.multi.datasources", Bindable
                    .mapOf(
                        String::class.java,
                        JDBCConnectionConfiguration::class.java
                    )
            ).orElse(emptyMap()), genProperties, signProperties, summerWebProperties
        )
    }

}
