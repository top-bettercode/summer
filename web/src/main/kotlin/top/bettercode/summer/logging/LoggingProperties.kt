package top.bettercode.summer.logging

import org.springframework.boot.context.properties.ConfigurationProperties

/**
 * @author Peter Wu
 */
@Suppress("ConfigurationProperties")
@ConfigurationProperties("summer.logging")
open class LoggingProperties {
    /**
     * 日志报警标题.
     */
    var warnSubject = "\${spring.application.name} \${summer.web.project-name:} \${spring.profiles.active}"

    /**
     * Log levels severity mapping. Use 'root' for the root logger.Stored in different files,
     * respectively.
     */
    var spilt: Map<String, String>? = null

    /**
     * Log levels severity mapping.Delimited files based on markup.
     */
    var spiltMarker: Map<String, String>? = null

    /**
     * Log levels.Stored in different files, respectively.
     */
    var spiltLevel: Array<String>? = null

    /**
     * 是否启用日志浏览功能.
     */
    var isShowEnabled = true

}
