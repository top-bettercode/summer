package top.bettercode.summer.tools.sms.aliyun

import org.springframework.boot.context.properties.ConfigurationProperties
import top.bettercode.summer.tools.lang.client.ClientProperties

@ConfigurationProperties(prefix = "summer.sms.aliyun")
open class AliSmsProperties : ClientProperties("短信平台") {
    //--------------------------------------------
    var url = "https://dysmsapi.aliyuncs.com"
    var accessKeyId: String? = null
    var accessKeySecret: String? = null
    var regionId = "cn-chengdu"

    /**
     * 模拟发送
     */
    var isMock = false
}
