package top.bettercode.summer.logging

import org.springframework.boot.context.properties.ConfigurationProperties
import top.bettercode.summer.tools.lang.util.RandomUtil.nextString

/**
 * RequestLogging 配置
 *
 * @author Peter Wu
 */
@ConfigurationProperties("summer.logging.websocket")
class WebsocketProperties {
    //--------------------------------------------
    /**
     * 是否启用
     */
    var isEnabled = true

    /**
     * 认证token
     */
    var token = nextString(16)
}