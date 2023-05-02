package top.bettercode.summer.tools.mobile

import org.springframework.boot.context.properties.ConfigurationProperties

/**
 * @author Peter Wu
 */
@ConfigurationProperties(prefix = "summer.mobile-query")
class MobileQueryProperties {
    /**
     * 接口地址
     */
    var url = "https://service-mxd4ekp3-1255701024.sh.apigw.tencentcs.com/release/superapi/super/flashsdk/mobile-query"

    /**
     * 应用标识
     */
    var appId: String = ""

    /**
     * 应用密钥
     */
    var appKey: String = ""

    /**
     * 请求连接超时时间毫秒数
     */
    var connectTimeout = 10000

    /**
     * 请求读取超时时间毫秒数
     */
    var readTimeout = 10000

}
