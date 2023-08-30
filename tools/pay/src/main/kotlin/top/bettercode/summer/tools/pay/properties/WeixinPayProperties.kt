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
    var apiKey: String? = null

    /**
     * 商户ID
     */
    var mchId: String? = null

    /**
     * 应用ID
     */
    var appid: String? = null

    /**
     * 通知地址
     */
    var notifyUrl: String? = null
    /**
     * 证书路径
     */
    var certPath: String? = null
    /**
     * 证书存储密码
     */
    var certStorePassword: String? = null
    /**
     * 证书Key密码
     */
    var certKeyPassword: String? = null

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