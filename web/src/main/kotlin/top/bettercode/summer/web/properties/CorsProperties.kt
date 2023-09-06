package top.bettercode.summer.web.properties

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.web.cors.CorsConfiguration

/**
 * @author Peter Wu
 * @since 0.1.12
 */
@ConfigurationProperties("summer.security.cors")
open class CorsProperties : CorsConfiguration() {
    var isEnable = true
    var path = "/**"

    init {
        allowedOrigins = listOf(ALL)
        allowedMethods = listOf(ALL)
        //    setAllowCredentials(true);
        allowedHeaders = listOf(ALL)
    }
}
