package top.bettercode.summer.apisign

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.web.servlet.error.ErrorController
import org.springframework.web.method.HandlerMethod

/**
 * 签名配置属性
 *
 * @author Peter Wu
 */
@ConfigurationProperties("summer.sign")
open class ApiSignProperties {
    /**
     * 验证签名时效时允许客户端与服务器的时差，单位秒 如果小于等于0 不验证签名时效.
     */
    var allowableClientTimeDifference = 0

    /**
     * 是否验证 userAgent.
     */
    var isVerifyUserAgent = false

    /**
     * 是否可跳过验证.
     */
    var isCanSkip = true
    //--------------------------------------------
    /**
     * 需要验证签名的 Controller类名前缀.
     */
    var handlerTypePrefix = arrayOf<String>()

    /**
     * 签名参数名.
     */
    var parameterName = "sign"

    /**
     * 默认客户端密码.
     */
    var clientSecret: String? = null
    val isSimple: Boolean
        //--------------------------------------------
        get() = !isVerifyUserAgent && allowableClientTimeDifference <= 0

    fun requiredSign(handler: Any?): Boolean {
        if (handler !is HandlerMethod) {
            return false
        }
        val handlerMethod = handler
        if (handlerMethod.bean is ErrorController) {
            return false
        }
        if (handlerMethod.hasMethodAnnotation(ApiSignIgnore::class.java)) {
            return false
        }
        val beanType = handlerMethod.beanType
        if (beanType.isAnnotationPresent(ApiSignIgnore::class.java)) {
            return false
        }
        val name = beanType.name
        for (typePrefix in handlerTypePrefix) {
            if (name.matches(("^" + typePrefix.replace(".", "\\.").replace("*", ".+") + ".*$").toRegex())) {
                return true
            }
        }
        return false
    }
}
