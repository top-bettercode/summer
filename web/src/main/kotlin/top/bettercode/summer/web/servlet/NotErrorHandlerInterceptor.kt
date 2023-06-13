package top.bettercode.summer.web.servlet

import org.springframework.boot.web.servlet.error.DefaultErrorAttributes
import org.springframework.boot.web.servlet.error.ErrorController
import org.springframework.lang.Nullable
import org.springframework.web.method.HandlerMethod
import org.springframework.web.servlet.AsyncHandlerInterceptor
import javax.servlet.RequestDispatcher
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

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
            val handlerMethod = handler
            return if (ErrorController::class.java.isAssignableFrom(handlerMethod.beanType)) {
                true
            } else preHandlerMethod(request, response, handlerMethod)
        }
        return true
    }

    fun preHandlerMethod(request: HttpServletRequest?, response: HttpServletResponse?,
                         handler: HandlerMethod?): Boolean {
        return true
    }

    override fun afterCompletion(request: HttpServletRequest, response: HttpServletResponse, handler: Any, @Nullable ex: java.lang.Exception?) {
        if (handler is HandlerMethod) {
            if (ErrorController::class.java.isAssignableFrom(handler.beanType)) {
                return
            }
            afterCompletionMethod(request, response, handler, ex)
        }
    }

    fun afterCompletionMethod(request: HttpServletRequest?, response: HttpServletResponse?,
                              handler: HandlerMethod?, ex: Throwable?) {
    }

    fun getError(request: HttpServletRequest): Throwable? {
        var exception = request.getAttribute(ERROR_INTERNAL_ATTRIBUTE) as Throwable?
        if (exception == null) {
            exception = request.getAttribute(RequestDispatcher.ERROR_EXCEPTION) as Throwable?
        }
        return exception
    }

    companion object {
        val ERROR_INTERNAL_ATTRIBUTE = DefaultErrorAttributes::class.java.name + ".ERROR"
    }
}
