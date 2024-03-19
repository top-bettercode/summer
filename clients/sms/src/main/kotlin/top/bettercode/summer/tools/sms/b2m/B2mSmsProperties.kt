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
    var isMock = false

    /**
     * 请求超时超过多少秒报警，-1表示不报警，默认-1.
     */
    var timeoutAlarmSeconds = -1

    /**
     * 请求连接超时时间秒数
     */
    var connectTimeout = 10

    /**
     * 请求读取超时时间秒数
     */
    var readTimeout = 10

    /**
     * 请求有效时间(必填)[单位:秒]
     */
    var requestValidPeriod = 60 * 10
}
