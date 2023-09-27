package top.bettercode.summer.web.properties

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.web.context.request.NativeWebRequest
import java.time.Duration
import javax.servlet.http.HttpServletRequest

/**
 * @author Peter Wu
 */
@ConfigurationProperties("summer.web")
open class SummerWebProperties {
    /**
     * 项目名称.
     */
    var projectName: String? = null

    /**
     * 响应结果是否包一层{\"data\":52,\"message\":\"\",\"status\":\"200\"}样式的格式.
     */
    var isWrapEnable = true

    /**
     * http响应状态码统一为200.
     */
    var isOkEnable = true

    /**
     * 接口版本号header参数名称.
     */
    var versionName = "api-version"

    /**
     * 接口版本号.
     */
    var version = "v1.0"

    /**
     * 接口版本号header参数名称.
     */
    var versionNoName = "api-version-no"

    /**
     * 接口版本号.
     */
    var versionNo = "1"

    /**
     * 字段效验异常信息分隔符.
     */
    var constraintViolationSeparator = ""

    /**
     * Response wrap header参数名称
     */
    var wrapName = "wrap-response"

    /**
     * Response ok header参数名称
     */
    var okName = "ok-response"

    /**
     * 表单防重复提交,header参数名称
     */
    var formKeyName = "formkey"
    //--------------------------------------------
    /**
     * 表单防重复提交，key有效时间
     */
    var formKeyTtl: Duration = Duration.ofSeconds(5)

    //--------------------------------------------
    fun wrapEnable(request: NativeWebRequest): Boolean {
        val wrapResponse = request.getHeader(wrapName)
        return if (!wrapResponse.isNullOrBlank()) {
            "true" == wrapResponse
        } else {
            isWrapEnable
        }
    }

    fun wrapEnable(request: HttpServletRequest): Boolean {
        val wrapResponse = request.getHeader(wrapName)
        return if (!wrapResponse.isNullOrBlank()) {
            "true" == wrapResponse
        } else {
            isWrapEnable
        }
    }

    fun okEnable(request: NativeWebRequest): Boolean {
        val okResponse = request.getHeader(okName)
        return if (!okResponse.isNullOrBlank()) {
            "true" == okResponse
        } else {
            isOkEnable
        }
    }

    fun okEnable(request: HttpServletRequest): Boolean {
        val okResponse = request.getHeader(okName)
        return if (!okResponse.isNullOrBlank()) {
            "true" == okResponse
        } else {
            isOkEnable
        }
    }
}
