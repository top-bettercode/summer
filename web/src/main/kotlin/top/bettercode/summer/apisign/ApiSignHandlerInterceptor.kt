package top.bettercode.summer.apisign

import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.core.Ordered
import org.springframework.web.method.HandlerMethod
import top.bettercode.summer.web.servlet.NotErrorHandlerInterceptor

/**
 * 签名验证拦截器

 * @author Peter Wu
 */
class ApiSignHandlerInterceptor(private var apiSignAlgorithm: ApiSignAlgorithm) :
        NotErrorHandlerInterceptor, Ordered {

    override fun getOrder(): Int {
        return Ordered.HIGHEST_PRECEDENCE + 20
    }

    override fun preHandlerMethod(request: HttpServletRequest, response: HttpServletResponse, handler: HandlerMethod): Boolean {
        if (apiSignAlgorithm.properties.requiredSign(handler)) {
            apiSignAlgorithm.checkSign(request)
        }

        return true
    }


}
