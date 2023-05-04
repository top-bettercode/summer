package top.bettercode.summer.logging

import ch.qos.logback.core.CoreConstants
import org.springframework.boot.context.properties.ConfigurationProperties

/**
 * logging file 配置
 *
 * @author Peter Wu
 */
@ConfigurationProperties("summer.logging.files")
open class FilesProperties {
    var path: String? = null
    var maxFileSize = "10MB"
    var maxHistory = CoreConstants.UNBOUND_HISTORY

    /**
     * 是否启动时滚动日志
     */
    var isRolloverOnStart = true
}