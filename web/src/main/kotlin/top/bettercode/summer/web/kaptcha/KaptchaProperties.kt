package top.bettercode.summer.web.kaptcha

import org.springframework.boot.context.properties.ConfigurationProperties

/**
 * @author Peter Wu
 */
@ConfigurationProperties("summer.kaptcha")
open class KaptchaProperties {
    //--------------------------------------------
    var border = "no"
    var textproducerFontColor = "black"
    var textproducerCharSpace = 5

    /**
     * 验证码过期时间，单位秒
     */
    var expireSeconds = 5 * 60
}
