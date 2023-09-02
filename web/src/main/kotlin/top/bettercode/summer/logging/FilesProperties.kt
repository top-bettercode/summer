package top.bettercode.summer.logging

import org.springframework.boot.context.properties.ConfigurationProperties

/**
 * logging file 配置
 *
 * @author Peter Wu
 */
@Suppress("ConfigurationProperties")
@ConfigurationProperties("summer.logging.files")
open class FilesProperties {
    var path: String? = null
    var maxFileSize = "10MB"
    var maxHistory = 0

    /**
     * 是否启动时滚动日志
     */
    var isRolloverOnStart = true
}