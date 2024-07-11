package top.bettercode.summer.logging.feishu

import org.springframework.boot.context.properties.ConfigurationProperties

/**
 * slack 配置
 *
 * @author Peter Wu
 */
@Suppress("ConfigurationProperties")
@ConfigurationProperties("summer.logging.feishu")
open class FeishuProperties {
    var logger = arrayOf("root")
    var ignoredWarnLogger = arrayOf<String>()
    var appId: String = ""
    var appSecret: String = ""
    var chat: String = ""
    var timeoutChat = "timeout"
    var cacheSeconds = 5 * 60L

    var timeoutCacheSeconds = 2 * 60 * 60L
    var cyclicBufferSize = 20
}