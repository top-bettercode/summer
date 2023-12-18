package top.bettercode.summer.web.servlet

import jakarta.servlet.RequestDispatcher
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.boot.web.servlet.error.DefaultErrorAttributes
import org.springframework.boot.web.servlet.error.ErrorController
import org.springframework.web.method.HandlerMethod
import org.springframework.web.servlet.AsyncHandlerInterceptor

/**
 * 非错误 HandlerMethod Interceptor
 *
 * @author Peter Wu
 */
interface NotErrorHandlerInterceptor : AsyncHandlerInterceptor {
    override fun preHandle(request: HttpServletRequest,
                           response: HttpServletResponse,
                           handler: Any): Boolean {
        if (handler is HandlerMethod) {
            return if (isErrorController(handler)) {
                true
            } else preHandlerMethod(request, response, handler)
        }
        return true
    }

    fun preHandlerMethod(request: HttpServletRequest, response: HttpServletResponse,
                         handler: HandlerMethod): Boolean {
        return true
    }

    override fun afterCompletion(request: HttpServletRequest, response: HttpServletResponse, handler: Any, ex: java.lang.Exception?) {
        if (handler is HandlerMethod) {
            if (isErrorController(handler)) {
                return
            }
            afterCompletionMethod(request, response, handler, ex)
        }
    }


    fun afterCompletionMethod(request: HttpServletRequest, response: HttpServletResponse,
                              handler: HandlerMethod, ex: Throwable?) {
    }

    companion object {

        @JvmStatic
        val ERROR_INTERNAL_ATTRIBUTE = DefaultErrorAttributes::class.java.name + ".ERROR"

        @JvmStatic
        fun getError(request: HttpServletRequest): Throwable? {
            var exception = request.getAttribute(ERROR_INTERNAL_ATTRIBUTE) as Throwable?
            if (exception == null) {
                exception = request.getAttribute(RequestDispatcher.ERROR_EXCEPTION) as Throwable?
            }
            return exception
        }

        @JvmStatic
        fun isErrorController(handler: Any) = handler is HandlerMethod &&
                ErrorController::class.java.isAssignableFrom(handler.beanType)

    }
}
