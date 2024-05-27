package top.bettercode.summer.tools.amap

import org.springframework.boot.context.properties.ConfigurationProperties
import top.bettercode.summer.tools.lang.client.ClientProperties

@ConfigurationProperties(prefix = "summer.amap")
open class AMapProperties : ClientProperties("高德地图") {
    //--------------------------------------------
    var url = "https://restapi.amap.com/v3"
    var key: String? = null

}
