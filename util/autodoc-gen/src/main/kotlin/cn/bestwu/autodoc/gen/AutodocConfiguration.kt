package cn.bestwu.autodoc.gen

import cn.bestwu.api.sign.ApiSignProperties
import cn.bestwu.logging.RequestLoggingConfiguration
import cn.bestwu.logging.RequestLoggingProperties
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
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
@ConditionalOnProperty(prefix = "autodoc.gen", name = ["enable"], havingValue = "true")
@EnableConfigurationProperties(GenProperties::class, ApiSignProperties::class)
@Configuration
@ImportAutoConfiguration(RequestLoggingConfiguration::class)
class AutodocConfiguration {
    @Autowired
    private lateinit var genProperties: GenProperties
    @Autowired
    private lateinit var requestLoggingProperties: RequestLoggingProperties
    @Autowired(required = false)
    private var dataSourceProperties: DataSourceProperties? = null

    @PostConstruct
    fun init() {
        if (genProperties.datasource.url.isBlank() && dataSourceProperties != null) {
            genProperties.datasource.url = dataSourceProperties!!.determineUrl() ?: ""
            genProperties.datasource.username = dataSourceProperties!!.determineUsername() ?: ""
            genProperties.datasource.password = dataSourceProperties!!.determinePassword() ?: ""
            genProperties.datasource.driverClass = dataSourceProperties!!.determineDriverClassName()
                    ?: ""
        }
        requestLoggingProperties.isFormat = true
        requestLoggingProperties.isForceRecord = true
        requestLoggingProperties.isIncludeRequestBody = true
        requestLoggingProperties.isIncludeResponseBody = true
        requestLoggingProperties.isIncludeTrace = true
    }

    @Bean
    fun autodocHandler(signProperties: ApiSignProperties, @Value("\${app.web.wrap.enable:false}") wrap: Boolean): AutodocHandler {
        return AutodocHandler(genProperties, signProperties, wrap)
    }

}
