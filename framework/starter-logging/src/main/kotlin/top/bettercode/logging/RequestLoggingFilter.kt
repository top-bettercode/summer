package top.bettercode.logging

import net.logstash.logback.marker.Markers
import org.apache.catalina.connector.ClientAbortException
import org.slf4j.LoggerFactory
import org.slf4j.MarkerFactory
import org.springframework.boot.web.servlet.error.DefaultErrorAttributes
import org.springframework.core.Ordered
import org.springframework.core.annotation.AnnotatedElementUtils
import org.springframework.http.HttpStatus
import org.springframework.util.AntPathMatcher
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.context.request.RequestAttributes
import org.springframework.web.context.request.ServletRequestAttributes
import org.springframework.web.filter.OncePerRequestFilter
import org.springframework.web.method.HandlerMethod
import org.springframework.web.servlet.HandlerMapping
import org.springframework.web.util.WebUtils
import top.bettercode.lang.util.StringUtil
import top.bettercode.logging.annotation.NoRequestLogging
import top.bettercode.logging.annotation.RequestLogging
import top.bettercode.logging.logback.AlarmMarker
import top.bettercode.logging.logback.Logback2LoggingSystem
import top.bettercode.logging.operation.Operation
import top.bettercode.logging.operation.RequestConverter
import top.bettercode.logging.operation.ResponseConverter
import top.bettercode.logging.trace.TraceHttpServletRequestWrapper
import top.bettercode.logging.trace.TraceHttpServletResponseWrapper
import top.bettercode.simpleframework.AnnotatedUtils
import top.bettercode.simpleframework.servlet.HandlerMethodContextHolder
import java.io.IOException
import java.time.LocalDateTime
import javax.servlet.FilterChain
import javax.servlet.ServletException
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

/**
 * 请求日志过滤器
 *
 * @author Peter Wu
 * @since 0.0.1
 */
class RequestLoggingFilter(
    private val properties: RequestLoggingProperties,
    private val handlers: List<RequestLoggingHandler>
) : OncePerRequestFilter(), Ordered {

    companion object {
        const val REQUEST_LOG_MARKER = "request"
        const val ALARM_LOG_MARKER = "alarm"
        const val NO_ALARM_LOG_MARKER = "no_alarm"
        const val OPERATION_MARKER = "operation"
        const val IS_OPERATION_MARKER = "is_operation"

        var API_HOST: String? = null

        val BEST_MATCHING_PATTERN_ATTRIBUTE =
            RequestLoggingFilter::class.java.name + ".bestMatchingPattern"
        val REQUEST_LOGGING_USERNAME = RequestLoggingFilter::class.java.name + ".username"
        val REQUEST_DATE_TIME = RequestLoggingFilter::class.java.name + ".dateTime"
    }

    private val log = LoggerFactory.getLogger(RequestLoggingFilter::class.java)

    @Throws(ServletException::class, IOException::class)
    override fun doFilterInternal(
        request: HttpServletRequest, response: HttpServletResponse,
        filterChain: FilterChain
    ) {
        if (API_HOST.isNullOrBlank()) {
            API_HOST = RequestConverter.getRequestPath(request)
        }

//        ignored
        val uri = request.servletPath
        if (properties.matchIgnored(uri)) {
            filterChain.doFilter(request, response)
            return
        }

        request.setAttribute(REQUEST_DATE_TIME, LocalDateTime.now())

        val requestToUse: HttpServletRequest = if (properties.isIncludeRequestBody) {
            TraceHttpServletRequestWrapper(request)
        } else {
            request
        }
        val responseToUse: HttpServletResponse = if (properties.isIncludeResponseBody) {
            TraceHttpServletResponseWrapper(
                response
            )
        } else {
            response
        }
        try {
            filterChain.doFilter(requestToUse, responseToUse)
        } finally {
            record(requestToUse, responseToUse, uri)
        }
    }

    private fun record(
        requestToUse: HttpServletRequest,
        responseToUse: HttpServletResponse,
        uri: String
    ) {
        if (!isAsyncStarted(requestToUse)) {
            val handler = HandlerMethodContextHolder.getHandler(requestToUse)
            val requestAttributes = ServletRequestAttributes(requestToUse)
            val error = getError(requestAttributes)
            val httpStatusCode = getStatus(requestAttributes)
            if (needRecord(requestToUse, handler, error, httpStatusCode, uri)) {
                val config: RequestLoggingConfig = requestLoggingConfig(requestToUse, handler)
                val operationResponse = ResponseConverter.convert(responseToUse)
                if (error != null) {
                    if (config.includeTrace) {
                        operationResponse.stackTrace = StringUtil.valueOf(error)
                    }
                }
                val operationRequest = RequestConverter.convert(requestToUse)


                val operation = Operation(
                    collectionName = config.collectionName,
                    name = config.operationName,
                    protocol = requestToUse.protocol,
                    request = operationRequest,
                    response = operationResponse
                )

                handlers.forEach {
                    //移动到生成日志消息之前，以便修改日志消息
                    try {
                        it.handle(operation, handler)
                    } catch (e: Exception) {
                        log.error(e.message, e)
                    }
                }

                val msg = operation.toString(config)
                if (!config.ignoredTimeout && config.timeoutAlarmSeconds > 0 && handler != null && operation.duration / 1000 > config.timeoutAlarmSeconds && !include(
                        properties.ignoredTimeoutPath,
                        uri
                    )
                ) {
                    val initialComment =
                        "${operation.collectionName}/${operation.name}(${operation.request.uri}) 请求响应速度慢"
                    val timeoutMsg = "：${operation.duration / 1000}秒"
                    val marker = MarkerFactory.getDetachedMarker(ALARM_LOG_MARKER)
                    marker.add(AlarmMarker(initialComment, timeoutMsg))
                    log.warn(marker, "$initialComment${timeoutMsg}\n$msg")
                }
                val marker = MarkerFactory.getDetachedMarker(REQUEST_LOG_MARKER)
                if (config.logMarker != REQUEST_LOG_MARKER) {
                    marker.add(MarkerFactory.getMarker(config.logMarker))
                }
                if (existProperty(environment, "summer.logging.logstash.destinations[0]")) {
                    marker.add(
                        Markers.appendRaw(
                            OPERATION_MARKER,
                            operation.toString(config.copy(format = false))
                        ).and(Markers.append("title", warnSubject(environment)))
                    )
                    marker.add(Markers.append(IS_OPERATION_MARKER, true))
                }
                if (error == null || error is ClientAbortException) {
                    log.info(marker, msg)
                } else {
                    if (!properties.ignoredErrorStatusCode.contains(httpStatusCode)) {
                        val initialComment = "$httpStatusCode ${error.javaClass.name}:${
                            error.message ?: getMessage(requestAttributes) ?: HttpStatus.INTERNAL_SERVER_ERROR.reasonPhrase
                        }"
                        marker.add(AlarmMarker(initialComment))
                        if (config.includeTrace)
                            log.error(marker, msg)
                        else
                            log.error(marker, msg, error)
                    } else
                        log.warn(marker, msg)
                }
            }
        }
    }

    private fun requestLoggingConfig(
        request: HttpServletRequest,
        handler: HandlerMethod?
    ): RequestLoggingConfig {
        request.setAttribute(
            BEST_MATCHING_PATTERN_ATTRIBUTE,
            request.getAttribute(HandlerMapping.BEST_MATCHING_PATTERN_ATTRIBUTE)
        )

        return if (handler != null) {
            val collectionName = AnnotatedElementUtils.getMergedAnnotation(
                handler.beanType,
                RequestMapping::class.java
            )?.name ?: ""
            val operationName = handler.getMethodAnnotation(RequestMapping::class.java)?.name ?: ""
            val requestLoggingAnno =
                AnnotatedUtils.getAnnotation(handler, RequestLogging::class.java)


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
            RequestLoggingConfig(
                includeRequestBody = properties.isIncludeRequestBody && requestLoggingAnno?.includeRequestBody != false,
                includeResponseBody = properties.isIncludeResponseBody && requestLoggingAnno?.includeResponseBody != false,
                includeTrace = properties.isIncludeTrace && requestLoggingAnno?.includeTrace != false,
                encryptHeaders = encryptHeaders ?: arrayOf(),
                encryptParameters = encryptParameters ?: arrayOf(),
                format = properties.isFormat,
                ignoredTimeout = requestLoggingAnno?.ignoredTimeout == true,
                timeoutAlarmSeconds = timeoutAlarmSeconds,
                logMarker = requestLoggingAnno?.logMarker ?: REQUEST_LOG_MARKER,
                collectionName = collectionName,
                operationName = operationName
            )
        } else
            RequestLoggingConfig(
                includeRequestBody = properties.isIncludeRequestBody,
                includeResponseBody = properties.isIncludeResponseBody,
                includeTrace = properties.isIncludeTrace,
                encryptHeaders = properties.encryptHeaders,
                encryptParameters = properties.encryptParameters,
                format = properties.isFormat,
                ignoredTimeout = false,
                timeoutAlarmSeconds = properties.timeoutAlarmSeconds,
                logMarker = REQUEST_LOG_MARKER,
                collectionName = "",
                operationName = ""
            )
    }


    private fun needRecord(
        request: HttpServletRequest,
        handler: HandlerMethod?,
        error: Throwable?,
        httpStatusCode: Int,
        uri: String
    ): Boolean {
        return if (properties.isForceRecord || handler != null ||
            include(properties.includePath, uri)
            || includeError(error) || !HttpStatus.valueOf(httpStatusCode).is2xxSuccessful
        ) {
            if (handler != null)
                (!AnnotatedUtils.hasAnnotation(
                    handler,
                    NoRequestLogging::class.java
                )) && useAnnotationMethodHandler(
                    request
                ) && (properties.handlerTypePrefix.isEmpty() || properties.handlerTypePrefix.any {
                    handler.beanType.name.packageMatches(
                        it
                    )
                })
            else false
        } else {
            false
        }
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


    private fun includeError(error: Throwable?): Boolean {
        return error != null && error !is ClientAbortException
    }

    private fun include(paths: Array<String>, servletPath: String): Boolean {
        val antPathMatcher = AntPathMatcher()
        for (path in paths) {
            if (antPathMatcher.match(path, servletPath)) {
                return true
            }
        }
        return false
    }

    private fun getStatus(requestAttributes: RequestAttributes): Int {
        val statusCode = getAttribute<Int>(requestAttributes, "javax.servlet.error.status_code")
        if (statusCode != null) {
            return statusCode
        }
        return HttpStatus.OK.value()
    }

    private fun getMessage(requestAttributes: RequestAttributes): String? {
        return getAttribute(requestAttributes, WebUtils.ERROR_MESSAGE_ATTRIBUTE)
    }

    private fun getError(requestAttributes: RequestAttributes): Throwable? {
        return getAttribute<Throwable>(
            requestAttributes,
            DefaultErrorAttributes::class.java.name + ".ERROR"
        )
    }

    @Suppress("UNCHECKED_CAST")
    private fun <T> getAttribute(requestAttributes: RequestAttributes, name: String): T? {
        return requestAttributes.getAttribute(name, RequestAttributes.SCOPE_REQUEST) as? T
    }

    override fun getOrder(): Int {
        return Ordered.HIGHEST_PRECEDENCE
    }

}
