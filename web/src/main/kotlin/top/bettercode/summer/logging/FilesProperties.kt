package top.bettercode.summer.logging

import org.springframework.boot.context.properties.ConfigurationProperties

/**
 * 配置 logging file
 *
 * @author Peter Wu
 */
@Suppress("ConfigurationProperties")
@ConfigurationProperties("summer.logging.files")
open class FilesProperties {
    /**
     * 查看路径
     */
    var viewPath: String? = null
    /**
     * 路径
     */
    var path: String? = null
    /**
     * 最大文件大小
     */
    var maxFileSize = "10MB"

    /**
     * 最大历史记录数量
     */
    var maxHistory = 100000

    /**
     * 启动时是否滚动日志
     */
    var isRolloverOnStart = true
}