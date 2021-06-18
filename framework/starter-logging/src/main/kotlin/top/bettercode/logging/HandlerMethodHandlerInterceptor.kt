package top.bettercode.logging

import top.bettercode.logging.annotation.NoRequestLogging
import org.springframework.boot.web.servlet.error.ErrorController
import org.springframework.core.annotation.AnnotatedElementUtils
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.method.HandlerMethod
import org.springframework.web.servlet.AsyncHandlerInterceptor
import org.springframework.web.servlet.HandlerMapping
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

/**
 * 保存 HandlerMethod
 *
 * @author Peter Wu
 */
class HandlerMethodHandlerInterceptor(private val properties: RequestLoggingProperties) :
    AsyncHandlerInterceptor {

    companion object {
        val HANDLER_METHOD = HandlerMethodHandlerInterceptor::class.java.name + ".handlerMethod"
        val BEST_MATCHING_PATTERN_ATTRIBUTE =
            HandlerMethodHandlerInterceptor::class.java.name + ".bestMatchingPattern"
        val COLLECTION_NAME = HandlerMethodHandlerInterceptor::class.java.name + ".collectionName"
        val OPERATION_NAME = HandlerMethodHandlerInterceptor::class.java.name + ".operationName"
        val REQUEST_LOGGING = HandlerMethodHandlerInterceptor::class.java.name + ".requestLogging"
    }

    override fun preHandle(
        request: HttpServletRequest,
        response: HttpServletResponse,
        handler: Any
    ): Boolean {
        if (handler is HandlerMethod && (
                    properties.isForceRecord
                            || (!AnnotatedUtils.hasAnnotation(
                        handler,
                        NoRequestLogging::class.java
                    ))
                    ) && handler.bean !is ErrorController && useAnnotationMethodHandler(
                request
            ) && (properties.handlerTypePrefix.isEmpty() || properties.handlerTypePrefix.any {
                handler.beanType.name.packageMatches(
                    it
                )
            })
        ) {
            request.setAttribute(HANDLER_METHOD, handler)
            request.setAttribute(
                BEST_MATCHING_PATTERN_ATTRIBUTE,
                request.getAttribute(HandlerMapping.BEST_MATCHING_PATTERN_ATTRIBUTE)
            )

            val collectionName = handler.beanType.getAnnotation(RequestMapping::class.java)?.name
                ?: ""
            request.setAttribute(COLLECTION_NAME, collectionName)
            val name = handler.getMethodAnnotation(RequestMapping::class.java)?.name ?: ""
            request.setAttribute(OPERATION_NAME, name)
            val requestLoggingAnno =
                (handler.getMethodAnnotation(top.bettercode.logging.annotation.RequestLogging::class.java)
                    ?: AnnotatedElementUtils.findMergedAnnotation(
                        handler.beanType,
                        top.bettercode.logging.annotation.RequestLogging::class.java
                    ))


            var encryptHeaders = requestLoggingAnno?.encryptHeaders
            if (encryptHeaders == null || encryptHeaders.isEmpty()) {
                encryptHeaders = properties.encryptHeaders
            }
            var encryptParameters = requestLoggingAnno?.encryptParameters
            if (encryptParameters == null || encryptParameters.isEmpty()) {
                encryptParameters = properties.encryptParameters
            }
            var timeoutAlarmSeconds = requestLoggingAnno?.timeoutAlarmSeconds ?: 0
            if (timeoutAlarmSeconds <= 0) {
                timeoutAlarmSeconds = properties.timeoutAlarmSeconds
            }
            val requestLogging = RequestLoggingConfig(
                includeRequestBody = properties.isIncludeRequestBody && requestLoggingAnno?.includeRequestBody != false,
                includeResponseBody = properties.isIncludeResponseBody && requestLoggingAnno?.includeResponseBody != false,
                includeTrace = properties.isIncludeTrace && requestLoggingAnno?.includeTrace != false,
                encryptHeaders = encryptHeaders ?: arrayOf(),
                encryptParameters = encryptParameters ?: arrayOf(),
                format = properties.isFormat,
                ignoredTimeout = requestLoggingAnno?.ignoredTimeout == true,
                timeoutAlarmSeconds = timeoutAlarmSeconds,
                logMarker = requestLoggingAnno?.logMarker ?: RequestLoggingFilter.REQUEST_LOG_MARKER
            )

            request.setAttribute(REQUEST_LOGGING, requestLogging)
        }
        return true
    }

    /**
     * 只记录 AnnotationMethodHandler 相关请求
     */
    private fun useAnnotationMethodHandler(request: HttpServletRequest): Boolean {
        val value = request.getAttribute(HandlerMapping.INTROSPECT_TYPE_LEVEL_MAPPING)
        return if (value != null) value as Boolean else java.lang.Boolean.TRUE
    }

    private fun String.packageMatches(regex: String) =
        matches(Regex("^" + regex.replace(".", "\\.").replace("*", ".+") + ".*$"))

}
