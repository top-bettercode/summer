package top.bettercode.summer.tools.weixin.properties

import org.springframework.boot.context.properties.ConfigurationProperties

/**
 * @author Peter Wu
 */
@ConfigurationProperties(prefix = "summer.wechat.mini")
class MiniprogramProperties : WexinProperties(), IMiniprogramProperties
