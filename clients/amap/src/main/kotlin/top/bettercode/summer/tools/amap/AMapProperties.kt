package top.bettercode.summer.tools.amap

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "summer.amap")
open class AMapProperties {
    //--------------------------------------------
    var url = "https://restapi.amap.com/v3"
    var key: String? = null

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
