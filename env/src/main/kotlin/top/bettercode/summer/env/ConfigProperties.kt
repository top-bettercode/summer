package top.bettercode.summer.env

import org.springframework.boot.context.properties.ConfigurationProperties

/**
 * @author Peter Wu
 */
@Suppress("ConfigurationProperties")
@ConfigurationProperties("summer.config.server.git")
open class ConfigProperties {

    /**
     * Git仓库地址
     */
    var uri: String? = null

    /**
     * Git仓库配置文件路径
     */
    var configFile: String? = null

    /**
     * Git仓库用户名
     */
    var username: String? = null

    /**
     * Git仓库密码
     */
    var password: String? = null
}
