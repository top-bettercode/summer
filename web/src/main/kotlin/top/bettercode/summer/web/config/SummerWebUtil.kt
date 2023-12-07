package top.bettercode.summer.web.config

import org.springframework.web.context.request.NativeWebRequest
import top.bettercode.summer.web.properties.SummerWebProperties
import javax.servlet.http.HttpServletRequest

/**
 *
 * @author Peter Wu
 */
object SummerWebUtil {

    fun SummerWebProperties.wrapEnable(request: NativeWebRequest): Boolean {
        val wrapResponse = request.getHeader(wrapName)
        return if (!wrapResponse.isNullOrBlank()) {
            "true" == wrapResponse
        } else {
            isWrapEnable
        }
    }

    fun SummerWebProperties.wrapEnable(request: HttpServletRequest): Boolean {
        val wrapResponse = request.getHeader(wrapName)
        return if (!wrapResponse.isNullOrBlank()) {
            "true" == wrapResponse
        } else {
            isWrapEnable
        }
    }

    fun SummerWebProperties.okEnable(request: NativeWebRequest): Boolean {
        val okResponse = request.getHeader(okName)
        return if (!okResponse.isNullOrBlank()) {
            "true" == okResponse
        } else {
            isOkEnable
        }
    }

    fun SummerWebProperties.okEnable(request: HttpServletRequest): Boolean {
        val okResponse = request.getHeader(okName)
        return if (!okResponse.isNullOrBlank()) {
            "true" == okResponse
        } else {
            isOkEnable
        }
    }

}