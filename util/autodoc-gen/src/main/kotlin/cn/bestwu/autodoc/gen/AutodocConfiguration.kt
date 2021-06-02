package cn.bestwu.autodoc.gen

import cn.bestwu.api.sign.ApiSignProperties
import cn.bestwu.logging.RequestLoggingConfiguration
import cn.bestwu.logging.RequestLoggingProperties
import cn.bestwu.simpleframework.config.WebProperties
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.autoconfigure.ImportAutoConfiguration
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import javax.annotation.PostConstruct

/**
 * @author Peter Wu
 */
@ConditionalOnProperty(prefix = "summer.autodoc.gen", name = ["enable"], havingValue = "true")
@EnableConfigurationProperties(GenProperties::class, ApiSignProperties::class)
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

    @PostConstruct
    fun init() {
        try {
            if (genProperties.datasource.url.isBlank() && dataSourceProperties != null) {
                genProperties.datasource.url = dataSourceProperties!!.determineUrl() ?: ""
                genProperties.datasource.username = dataSourceProperties!!.determineUsername() ?: ""
                genProperties.datasource.password = dataSourceProperties!!.determinePassword() ?: ""
                genProperties.datasource.driverClass = dataSourceProperties!!.determineDriverClassName()
                        ?: ""
            }
        } catch (e: Exception) {
            log.warn("determine determine fail: {}", e.message)
        }
        requestLoggingProperties.isFormat = true
        requestLoggingProperties.isForceRecord = true
        requestLoggingProperties.isIncludeRequestBody = true
        requestLoggingProperties.isIncludeResponseBody = true
        requestLoggingProperties.isIncludeTrace = true
    }

    @Bean
    fun autodocHandler(signProperties: ApiSignProperties, webProperties: WebProperties): AutodocHandler {
        return AutodocHandler(genProperties, signProperties, webProperties)
    }

}
