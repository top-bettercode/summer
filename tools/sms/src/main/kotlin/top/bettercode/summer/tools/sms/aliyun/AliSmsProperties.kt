package top.bettercode.summer.tools.sms.aliyun

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "summer.sms.aliyun")
class AliSmsProperties {
    //--------------------------------------------
    var url = "https://dysmsapi.aliyuncs.com"
    var accessKeyId: String? = null
    var accessKeySecret: String? = null
    var regionId = "cn-chengdu"

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
}
