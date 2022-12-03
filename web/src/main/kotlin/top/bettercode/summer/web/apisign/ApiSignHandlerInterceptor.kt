package top.bettercode.summer.web.apisign

import org.springframework.core.Ordered
import org.springframework.web.method.HandlerMethod
import top.bettercode.summer.web.servlet.NotErrorHandlerInterceptor
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

/**
 * 签名验证拦截器

 * @author Peter Wu
 */
class ApiSignHandlerInterceptor(private var apiSignAlgorithm: ApiSignAlgorithm) :
    NotErrorHandlerInterceptor, Ordered {

    override fun getOrder(): Int {
        return Ordered.HIGHEST_PRECEDENCE + 20
    }

    override fun preHandlerMethod(
        request: HttpServletRequest,
        response: HttpServletResponse,
        handler: HandlerMethod
    ): Boolean {
        if (apiSignAlgorithm.properties.requiredSign(handler)) {
            apiSignAlgorithm.checkSign(request)
        }

        return true
    }


}
