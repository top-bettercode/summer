package top.bettercode.summer.tools.rapidauth

import org.springframework.boot.context.properties.ConfigurationProperties
import top.bettercode.summer.tools.lang.client.ClientProperties

/**
 * @author Peter Wu
 */
@ConfigurationProperties(prefix = "summer.rapidauth")
open class RapidauthProperties :ClientProperties("腾讯云号码认证"){
    /**
     * 接口地址
     */
    var url = "https://yun.tim.qq.com/v5/rapidauth/validate?sdkappid={0}&random={1}"

    /**
     * 腾讯应用ID
     */
    var sdkappid: String? = null

    /**
     * 应用密钥
     */
    var appkey: String? = null
}
