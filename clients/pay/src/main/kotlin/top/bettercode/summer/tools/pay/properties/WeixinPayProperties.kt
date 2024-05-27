package top.bettercode.summer.tools.pay.properties

import org.springframework.boot.context.properties.ConfigurationProperties
import top.bettercode.summer.tools.lang.client.ClientProperties

/**
 * 微售支付配置
 */
@ConfigurationProperties(prefix = "summer.pay.weixin")
data class WeixinPayProperties @JvmOverloads constructor(
        /**
         * 商户ID
         */
        var mchId: String? = null,

        /**
         * 应用ID
         */
        var appid: String? = null,

        /**
         * API密钥
         */
        var apiKey: String? = null,

        /**
         * 通知地址
         */
        var notifyUrl: String? = null,
        /**
         * 证书路径
         */
        var certLocation: String? = null,
        /**
         * 证书存储密码
         */
        var certStorePassword: String? = null,
        /**
         * 证书Key密码
         */
        var certKeyPassword: String? = null,
):ClientProperties("微信支付")