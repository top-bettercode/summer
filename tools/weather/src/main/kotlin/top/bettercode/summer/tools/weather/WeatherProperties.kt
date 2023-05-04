package top.bettercode.summer.tools.weather

import org.springframework.boot.context.properties.ConfigurationProperties
import java.time.LocalTime

/**
 * @author Peter Wu
 */
@ConfigurationProperties(prefix = "summer.weather")
open class WeatherProperties {
    /**
     * 接口地址
     */
    var url = "https://sapi.k780.com"
        private set

    /**
     * 使用API的唯一凭证
     */
    var appKey: String? = null
        private set

    /**
     * md5后的32位密文,登陆用.
     */
    var sign: String? = null

    /**
     * 晚上起始时间
     */
    var nightStartTime: LocalTime = LocalTime.of(18, 0)

    /**
     * 晚上结束时间
     */
    var nightEndTime: LocalTime = LocalTime.of(6, 0)

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

    fun setUrl(url: String): WeatherProperties {
        this.url = url
        return this
    }

    fun setAppKey(appKey: String?): WeatherProperties {
        this.appKey = appKey
        return this
    }

    fun setConnectTimeout(connectTimeout: Int): WeatherProperties {
        this.connectTimeout = connectTimeout
        return this
    }

    fun setReadTimeout(readTimeout: Int): WeatherProperties {
        this.readTimeout = readTimeout
        return this
    }
}
