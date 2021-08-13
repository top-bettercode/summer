package top.bettercode.api.sign

import org.springframework.boot.web.servlet.error.ErrorController
import org.springframework.core.Ordered
import org.springframework.web.method.HandlerMethod
import org.springframework.web.servlet.AsyncHandlerInterceptor
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

/**
 * 签名验证拦截器

 * @author Peter Wu
 */
class ApiSignHandlerInterceptor(private var apiSignAlgorithm: ApiSignAlgorithm) :
    AsyncHandlerInterceptor, Ordered {

    override fun getOrder(): Int {
        return Ordered.HIGHEST_PRECEDENCE + 20
    }

    override fun preHandle(
        request: HttpServletRequest,
        response: HttpServletResponse,
        handler: Any
    ): Boolean {
        if (handler is HandlerMethod && ErrorController::class.java.isAssignableFrom(handler.beanType)) {
            return true
        }

        if (apiSignAlgorithm.properties.requiredSign(handler)) {
            apiSignAlgorithm.checkSign(request)
        }

        return true
    }


}
