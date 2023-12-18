package top.bettercode.summer.tools.rapidauth

import org.springframework.boot.context.properties.ConfigurationProperties

/**
 * @author Peter Wu
 */
@ConfigurationProperties(prefix = "summer.rapidauth")
open class RapidauthProperties {
    /**
     * 接口地址
     */
    var url = "https://yun.tim.qq.com/v5/rapidauth/validate?sdkappid={0}&random={1}"


    /**
     * 腾讯应用ID
     */
    var sdkappid: String? = null

    /**
     * 应用密钥
     */
    var appkey: String? = null

    /**
     * 请求连接超时时间毫秒数
     */
    var connectTimeout = 10000


    /**
     * 请求读取超时时间毫秒数
     */
    var readTimeout = 10000
}
