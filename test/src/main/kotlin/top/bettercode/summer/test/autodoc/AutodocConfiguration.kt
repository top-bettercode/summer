package top.bettercode.summer.test.autodoc

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
import top.bettercode.summer.apisign.ApiSignProperties
import top.bettercode.summer.logging.RequestLoggingConfiguration
import top.bettercode.summer.logging.RequestLoggingProperties
import top.bettercode.summer.tools.generator.GeneratorExtension.Companion.DEFAULT_MODULE_NAME
import top.bettercode.summer.tools.generator.DatabaseConfiguration
import top.bettercode.summer.web.properties.SummerWebProperties
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
        val datasources: MutableMap<String, DatabaseConfiguration> = Binder.get(
                environment
        ).bind<MutableMap<String, DatabaseConfiguration>>(
                "summer.datasource.multi.datasources", Bindable
                .mapOf(
                        String::class.java,
                        DatabaseConfiguration::class.java
                )
        ).orElse(mutableMapOf())
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
                    val configuration = DatabaseConfiguration()
                    configuration.url = dataSourceProperties!!.determineUrl() ?: ""
                    configuration.username = dataSourceProperties!!.determineUsername() ?: ""
                    configuration.password = dataSourceProperties!!.determinePassword() ?: ""
                    configuration.driverClass =
                            dataSourceProperties!!.determineDriverClassName() ?: ""
                    configuration.entityPrefix = genProperties.entityPrefix
                    configuration.tablePrefixes = genProperties.tablePrefixes
                    configuration.tableSuffixes = genProperties.tableSuffixes

                    datasources[DEFAULT_MODULE_NAME] = configuration
                }
            } catch (_: Exception) {
            }
        }

        return AutodocHandler(datasources, genProperties, signProperties, summerWebProperties)
    }

}
