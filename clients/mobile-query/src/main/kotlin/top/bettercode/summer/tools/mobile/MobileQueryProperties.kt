package top.bettercode.summer.tools.mobile

import org.springframework.boot.context.properties.ConfigurationProperties
import top.bettercode.summer.tools.lang.client.ClientProperties

/**
 * @author Peter Wu
 */
@ConfigurationProperties(prefix = "summer.mobile-query")
open class MobileQueryProperties : ClientProperties("号码平台") {
    /**
     * 接口地址
     */
    var url =
        "https://service-mxd4ekp3-1255701024.sh.apigw.tencentcs.com/release/superapi/super/flashsdk/mobile-query"

    /**
     * 应用标识
     */
    var appId: String = ""

    /**
     * 应用密钥
     */
    var appKey: String = ""

}
