package top.bettercode.summer.tools.sms.b2m

import org.springframework.boot.context.properties.ConfigurationProperties

/**
 * 亿美软通短信平台 配置信息
 *
 * @author Peter Wu
 */
@ConfigurationProperties(prefix = "summer.sms.b2m")
open class B2mSmsProperties {
    var url = "http://www.btom.cn:8080"
    var appId: String = ""
    var secretKey: String = ""

    /**
     * 模拟发送
     */
    var mock = false

    /**
     * 请求连接超时时间毫秒数
     */
    var connectTimeout = 10000

    /**
     * 请求读取超时时间毫秒数
     */
    var readTimeout = 10000

    /**
     * 请求有效时间(必填)[单位:秒]
     */
    var requestValidPeriod = 60 * 10
}
