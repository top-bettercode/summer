package top.bettercode.lang.servlet

import org.springframework.web.servlet.AsyncHandlerInterceptor
import org.springframework.boot.web.servlet.error.ErrorController
import org.springframework.web.method.HandlerMethod
import java.lang.Exception
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

/**
 * 非错误 HandlerMethod Interceptor
 *
 * @author Peter Wu
 */
interface NotErrorHandlerInterceptor : AsyncHandlerInterceptor {

    @Throws(Exception::class)
    @JvmDefault
    override fun preHandle(
        request: HttpServletRequest, response: HttpServletResponse,
        handler: Any
    ): Boolean {
        if (handler is HandlerMethod) {
            return if (ErrorController::class.java.isAssignableFrom(handler.beanType)) {
                true
            } else preHandlerMethod(request, response, handler)
        }
        return true
    }

    @Throws(Exception::class)
    fun preHandlerMethod(
        request: HttpServletRequest, response: HttpServletResponse,
        handler: HandlerMethod
    ): Boolean {
        return true
    }
}