package top.bettercode.summer.tools.amap

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "summer.amap")
open class AmapProperties {
    //--------------------------------------------
    var url = "https://restapi.amap.com/v3"
    var key: String? = null

    /**
     * 请求连接超时时间毫秒数
     */
    var connectTimeout = 10000

    /**
     * 请求读取超时时间毫秒数
     */
    var readTimeout = 10000
}
