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

    /**
     * 海康vision 门禁事件每页数据
     */
    var pageSize = 1000

    /**
     * 海康vision 门禁事件类型
     * 197127:指纹比对通过
     * 196893:人脸认证通过
     * 196887:指纹+密码认证通过
     */
    var eventTypes = arrayOf(197127, 196893, 196887)

}
