package top.bettercode.summer.tools.pay.properties

import org.springframework.boot.context.properties.ConfigurationProperties

/**
 * 微售支付配置
 */
@ConfigurationProperties(prefix = "summer.pay.weixin-v3")
data class WeixinV3PayProperties @JvmOverloads constructor(

        /**
         * 商户ID
         */
        var merchantId: String? = null,

        /**
         * 应用ID
         */
        var appid: String? = null,
        /**
         * apiV3Key密钥
         */
        var apiV3Key: String? = null,

        /**
         * 商户 API 私钥证书路径
         */
        var privateKeyPath: String? = null,
        /**
         * 平台证书序列号
         */
        var merchantSerialNumber: String? = null,
        /**
         * 请求连接超时时间毫秒数
         */
        var maxIdleConnections: Int = 5,
        /**
         * 请求读取超时时间毫秒数
         */
        var keepAliveDuration: Long = 7L,
        /**
         * 请求连接超时时间毫秒数
         */
        var connectTimeout: Long = 10000L,

        /**
         * 请求读取超时时间毫秒数
         */
        var readTimeout: Long = 10000L
)