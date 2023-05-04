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
        private set

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
        private set

    /**
     * 请求读取超时时间毫秒数
     */
    var readTimeout = 10000
        private set

    fun setUrl(url: String): RapidauthProperties {
        this.url = url
        return this
    }

    fun setConnectTimeout(connectTimeout: Int): RapidauthProperties {
        this.connectTimeout = connectTimeout
        return this
    }

    fun setReadTimeout(readTimeout: Int): RapidauthProperties {
        this.readTimeout = readTimeout
        return this
    }
}
