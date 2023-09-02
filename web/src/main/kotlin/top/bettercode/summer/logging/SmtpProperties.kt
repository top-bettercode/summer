package top.bettercode.summer.logging

import org.springframework.boot.context.properties.ConfigurationProperties

/**
 * logging smtp 配置
 *
 * @author Peter Wu
 */
@Suppress("ConfigurationProperties")
@ConfigurationProperties("summer.logging.smtp")
open class SmtpProperties {
    var logger = arrayOf("root")
    var from: String? = null
    var marker: String? = null
    var to: String? = null
    var username: String? = null
    var password: String? = null
    var filter = "ERROR"
    var localhost: String? = null
    var host: String? = null
    var port = 25
    var isStarttls = false
    var isSsl = false
    var isSessionViaJNDI = false
    var jndiLocation = "java:comp/env/mail/Session"
    var isIncludeCallerData = false
    var isAsynchronousSending = true
    var charsetEncoding = "UTF-8"
}