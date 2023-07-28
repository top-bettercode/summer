package top.bettercode.summer.logging

import org.springframework.boot.context.properties.ConfigurationProperties

/**
 * slack 配置
 *
 * @author Peter Wu
 */
@ConfigurationProperties("summer.logging.slack")
open class SlackProperties {
    var logger = arrayOf("root")
    var ignoredWarnLogger = arrayOf<String>()
    var authToken: String = ""
    var channel: String = ""
    var timeoutChannel = "timeout"
    var cacheSeconds = 5 * 60L
        private set
    var timeoutCacheSeconds = 2 * 60 * 60L
    var cyclicBufferSize = 20
    fun setCacheSeconds(cacheSeconds: Long): SlackProperties {
        this.cacheSeconds = cacheSeconds
        return this
    }
}