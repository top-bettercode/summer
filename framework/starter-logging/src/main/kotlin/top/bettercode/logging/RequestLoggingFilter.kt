package top.bettercode.logging

import net.logstash.logback.marker.Markers
import org.apache.catalina.connector.ClientAbortException
import org.slf4j.LoggerFactory
import org.slf4j.MDC
import org.slf4j.MarkerFactory
import org.springframework.boot.web.servlet.error.DefaultErrorAttributes
import org.springframework.core.Ordered
import org.springframework.http.HttpStatus
import org.springframework.util.AntPathMatcher
import org.springframework.web.context.request.RequestAttributes
import org.springframework.web.context.request.ServletRequestAttributes
import org.springframework.web.filter.OncePerRequestFilter
import org.springframework.web.method.HandlerMethod
import org.springframework.web.util.WebUtils
import top.bettercode.lang.util.StringUtil
import top.bettercode.logging.operation.Operation
import top.bettercode.logging.operation.RequestConverter
import top.bettercode.logging.operation.ResponseConverter
import top.bettercode.logging.slack.SlackClient
import top.bettercode.logging.trace.TraceHttpServletRequestWrapper
import top.bettercode.logging.trace.TraceHttpServletResponseWrapper
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

        val TIMEOUT_MSG = RequestLoggingFilter::class.java.name + ".timeout_msg"
        val REQUEST_LOGGING_USERNAME = RequestLoggingFilter::class.java.name + ".username"
        val REQUEST_DATE_TIME = RequestLoggingFilter::class.java.name + ".dateTime"
    }

    private val log = LoggerFactory.getLogger(RequestLoggingFilter::class.java)

    @Throws(ServletException::class, IOException::class)
    override fun doFilterInternal(
        request: HttpServletRequest, response: HttpServletResponse,
        filterChain: FilterChain
    ) {
        if (SlackClient.LOG_URL.isNullOrBlank()) {
            val logPath =
                environment.getProperty("management.endpoints.web.base-path") ?: "/actuator"
            SlackClient.LOG_URL = RequestConverter.getRequestPath(request) + logPath
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
            MDC.remove(WebUtils.ERROR_MESSAGE_ATTRIBUTE)
            record(requestToUse, responseToUse, uri)
        }
    }

    private fun record(
        requestToUse: HttpServletRequest,
        responseToUse: HttpServletResponse,
        uri: String
    ) {
        if (!isAsyncStarted(requestToUse)) {
            val handler =
                requestToUse.getAttribute(HandlerMethodHandlerInterceptor.HANDLER_METHOD) as? HandlerMethod
            val requestAttributes = ServletRequestAttributes(requestToUse)
            val error = getError(requestAttributes)
            val httpStatusCode = getStatus(requestAttributes)
            if (handler != null ||
                include(properties.includePath, uri)
                || includeError(error) || !HttpStatus.valueOf(httpStatusCode).is2xxSuccessful
            ) {
                val config: RequestLoggingConfig =
                    requestToUse.getAttribute(HandlerMethodHandlerInterceptor.REQUEST_LOGGING) as? RequestLoggingConfig
                        ?: RequestLoggingConfig(
                            includeRequestBody = properties.isIncludeRequestBody,
                            includeResponseBody = properties.isIncludeResponseBody,
                            includeTrace = properties.isIncludeTrace,
                            encryptHeaders = properties.encryptHeaders,
                            encryptParameters = properties.encryptParameters,
                            format = properties.isFormat,
                            ignoredTimeout = false,
                            timeoutAlarmSeconds = properties.timeoutAlarmSeconds
                        )
                val operationResponse = ResponseConverter.convert(responseToUse)
                if (error != null) {
                    if (config.includeTrace) {
                        operationResponse.stackTrace = StringUtil.valueOf(error)
                    }
                }
                val operationRequest = RequestConverter.convert(requestToUse)


                val operation = Operation(
                    collectionName = requestToUse.getAttribute(HandlerMethodHandlerInterceptor.COLLECTION_NAME) as? String
                        ?: "",
                    name = requestToUse.getAttribute(HandlerMethodHandlerInterceptor.OPERATION_NAME) as? String
                        ?: "",
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
                if (!config.ignoredTimeout && config.timeoutAlarmSeconds > 0 && handler != null && operation.duration > config.timeoutAlarmSeconds * 1000 && !include(
                        properties.ignoredTimeoutPath,
                        uri
                    )
                ) {
                    val timeoutLog =
                        "${operation.collectionName}/${operation.name}(${operation.request.uri}) 请求超时"
                    val timeout = "：${operation.duration}毫秒"
                    MDC.put(WebUtils.ERROR_MESSAGE_ATTRIBUTE, timeoutLog)
                    MDC.put(TIMEOUT_MSG, timeout)
                    log.warn(
                        MarkerFactory.getMarker(ALARM_LOG_MARKER),
                        "$timeoutLog${timeout}\n$msg"
                    )
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
                        val message = "$httpStatusCode ${error.javaClass.name}:${
                            getMessage(requestAttributes)
                                ?: error.message ?: HttpStatus.INTERNAL_SERVER_ERROR.reasonPhrase
                        }"
                        MDC.put(WebUtils.ERROR_MESSAGE_ATTRIBUTE, message)
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
