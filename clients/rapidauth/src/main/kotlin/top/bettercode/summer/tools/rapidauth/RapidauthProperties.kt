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
