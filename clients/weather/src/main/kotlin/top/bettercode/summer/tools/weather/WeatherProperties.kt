package top.bettercode.summer.tools.weather

import org.springframework.boot.context.properties.ConfigurationProperties
import top.bettercode.summer.tools.lang.client.ClientProperties
import java.time.LocalTime

/**
 * @author Peter Wu
 */
@ConfigurationProperties(prefix = "summer.weather")
open class WeatherProperties : ClientProperties(
    "天气数据平台",
) {
    /**
     * 接口地址
     */
    var url = "https://sapi.k780.com"

    /**
     * 使用API的唯一凭证
     */
    var appKey: String? = null

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

}
