package top.bettercode.summer.tools.pay.properties

import org.springframework.boot.context.properties.ConfigurationProperties

/**
 * 微售支付配置
 */
@ConfigurationProperties(prefix = "summer.pay")
class WeixinPayProperties {
    /**
     * API密钥
     */
    var apiKey: String = ""

    /**
     * 商户ID
     */
    var mchId: String = ""

    /**
     * 应用ID
     */
    var appid: String = ""

    /**
     * 通知地址
     */

    /**
     * 请求连接超时时间毫秒数
     */
    var connectTimeout = 10000

    /**
     * 请求读取超时时间毫秒数
     */
    var readTimeout = 10000

}