package top.bettercode.summer.tools.sap.config

import org.springframework.boot.context.properties.ConfigurationProperties

/**
 * @author Peter Wu
 */
@ConfigurationProperties("summer.sap")
open class SapProperties {
    var ashost: String? = null
    var sysnr: String? = null
    var client: String? = null
    var user: String? = null
    var passwd: String? = null
    var lang: String? = null
    var poolCapacity: String? = null
    var peakLimit: String? = null
}
