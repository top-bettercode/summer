package top.bettercode.summer.tools.sms.aliyun

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "summer.sms.aliyun")
open class AliSmsProperties {
    //--------------------------------------------
    var url = "https://dysmsapi.aliyuncs.com"
    var accessKeyId: String? = null
    var accessKeySecret: String? = null
    var regionId = "cn-chengdu"

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
}
