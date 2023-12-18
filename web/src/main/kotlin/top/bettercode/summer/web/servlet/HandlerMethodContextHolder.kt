package top.bettercode.summer.web.servlet

import org.slf4j.LoggerFactory
import org.springframework.boot.web.servlet.error.ErrorController
import org.springframework.http.HttpMethod
import org.springframework.web.method.HandlerMethod
import org.springframework.web.servlet.HandlerMapping
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping
import top.bettercode.summer.tools.lang.operation.HttpOperation
import top.bettercode.summer.tools.lang.operation.RequestConverter
import top.bettercode.summer.tools.lang.util.AnnotatedUtils.getAnnotation
import top.bettercode.summer.tools.lang.util.AnnotatedUtils.hasAnnotation
import javax.servlet.http.HttpServletRequest

/**
 * @author Peter Wu
 */
object HandlerMethodContextHolder {
    private val log = LoggerFactory.getLogger(HandlerMethodContextHolder::class.java)
    private val HANDLER_METHOD = HandlerMethodContextHolder::class.java.name + ".handlerMethod"
    private var handlerMapping: RequestMappingHandlerMapping? = null
    fun setHandlerMapping(
        handlerMapping: RequestMappingHandlerMapping?
    ) {
        HandlerMethodContextHolder.handlerMapping = handlerMapping
    }

    @JvmStatic
    fun getHandler(request: HttpServletRequest): HandlerMethod? {
        try {
            var handler = request.getAttribute(HandlerMapping.BEST_MATCHING_HANDLER_ATTRIBUTE)
            if (handler is HandlerMethod && !ErrorController::class.java.isAssignableFrom(
                    handler.beanType
                )
            ) {
                return handler
            }
            var handlerMethod: HandlerMethod?
            handlerMethod = request.getAttribute(HANDLER_METHOD) as HandlerMethod?
            if (handlerMethod != null) {
                return handlerMethod
            }
//            if (!ServletRequestPathUtils.hasParsedRequestPath(request)) {
//                ServletRequestPathUtils.parseAndCache(request)
//            }
            val handlerExecutionChain = handlerMapping!!.getHandler(request)
            if (handlerExecutionChain != null) {
                handler = handlerExecutionChain.handler
                if (handler is HandlerMethod && !ErrorController::class.java.isAssignableFrom(
                        handler.beanType
                    )
                ) {
                    handlerMethod = handler
                    request.setAttribute(HANDLER_METHOD, handlerMethod)
                    return handlerMethod
                }
            }
        } catch (e: Exception) {
            if (HttpMethod.OPTIONS.name != request.method) {
                val requestString = HttpOperation.toString(
                    request = RequestConverter.convert(request)
                )
                log.warn("\n$requestString", e)
            }
        }
        return null
    }

    fun <A : Annotation?> hasAnnotation(
        request: HttpServletRequest,
        annotationType: Class<A>
    ): Boolean {
        val handler = getHandler(request)
        return handler != null && hasAnnotation(handler, annotationType)
    }

    fun <A : Annotation?> getAnnotation(
        request: HttpServletRequest,
        annotationType: Class<A>
    ): A? {
        val handler = getHandler(request)
        return if (handler == null) null else getAnnotation(handler, annotationType)
    }
}