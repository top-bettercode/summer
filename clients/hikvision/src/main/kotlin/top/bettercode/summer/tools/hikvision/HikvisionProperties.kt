package top.bettercode.summer.tools.hikvision

import org.springframework.boot.context.properties.ConfigurationProperties
import top.bettercode.summer.tools.lang.client.ClientProperties

/**
 * @author Peter Wu
 */
@ConfigurationProperties(prefix = "summer.hikvision")
open class HikvisionProperties : ClientProperties("海康门禁") {
    /**
     * 平台的地址
     */
    var host: String = ""

    /**
     * 密钥appkey
     */
    var appKey: String = ""

    /**
     * 密钥appSecret
     */
    var appSecret: String = ""

    /**
     * OpenAPI接口的上下文
     */
    var artemisPath: String = "/artemis"
}
