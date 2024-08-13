package top.bettercode.summer.test.autodoc

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.autoconfigure.ImportAutoConfiguration
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.env.Environment
import top.bettercode.summer.apisign.ApiSignProperties
import top.bettercode.summer.config.PumlEndpoint
import top.bettercode.summer.logging.RequestLoggingConfiguration
import top.bettercode.summer.logging.RequestLoggingProperties
import top.bettercode.summer.tools.generator.DatabaseConfiguration
import top.bettercode.summer.tools.generator.GeneratorExtension.Companion.DEFAULT_MODULE_NAME
import top.bettercode.summer.web.properties.SummerWebProperties
import javax.annotation.PostConstruct

/**
 * @author Peter Wu
 */
@ConditionalOnProperty(prefix = "summer.autodoc.gen", name = ["enable"], havingValue = "true")
@EnableConfigurationProperties(
    GenProperties::class,
    ApiSignProperties::class
)
@ConditionalOnWebApplication
@Configuration(proxyBeanMethods = false)
@ImportAutoConfiguration(RequestLoggingConfiguration::class)
class AutodocConfiguration(
    private val genProperties: GenProperties,
    private val requestLoggingProperties: RequestLoggingProperties,
    private val environment: Environment,
    @Autowired(required = false)
    private val dataSourceProperties: DataSourceProperties? = null
) {

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
        summerWebProperties: SummerWebProperties,
        @Autowired(required = false) autodocAspect: AutodocAspect?
    ): AutodocHandler {
        val datasources: MutableMap<String, DatabaseConfiguration> =
            PumlEndpoint.databases(environment)

        datasources.values.forEach { configuration ->
            if (configuration.entityPrefix.isBlank())
                configuration.entityPrefix = genProperties.entityPrefix
            if (configuration.tablePrefixes.isEmpty())
                configuration.tablePrefixes = genProperties.tablePrefixes
            if (configuration.tableSuffixes.isEmpty())
                configuration.tableSuffixes = genProperties.tableSuffixes
        }
        val defaultConfiguration = datasources["primary"]
        if (defaultConfiguration != null) {
            datasources[DEFAULT_MODULE_NAME] = defaultConfiguration
        } else {
            try {
                if (dataSourceProperties != null) {
                    val database = DatabaseConfiguration()
                    database.url = dataSourceProperties.determineUrl() ?: ""
                    database.username = dataSourceProperties.determineUsername() ?: ""
                    database.password = dataSourceProperties.determinePassword() ?: ""
                    database.driverClass = dataSourceProperties.determineDriverClassName() ?: ""
                    database.entityPrefix = genProperties.entityPrefix
                    database.tablePrefixes = genProperties.tablePrefixes
                    database.tableSuffixes = genProperties.tableSuffixes

                    datasources[DEFAULT_MODULE_NAME] = database
                }
            } catch (_: Exception) {
            }
        }

        return AutodocHandler(
            datasources,
            genProperties,
            signProperties,
            summerWebProperties,
            autodocAspect
        )
    }

}
