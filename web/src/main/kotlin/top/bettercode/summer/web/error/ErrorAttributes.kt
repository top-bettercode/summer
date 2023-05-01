package top.bettercode.summer.web.error

import org.slf4j.LoggerFactory
import org.springframework.boot.autoconfigure.web.ErrorProperties
import org.springframework.boot.autoconfigure.web.ErrorProperties.IncludeAttribute
import org.springframework.boot.web.error.ErrorAttributeOptions
import org.springframework.boot.web.servlet.error.DefaultErrorAttributes
import org.springframework.context.MessageSource
import org.springframework.core.Ordered
import org.springframework.core.annotation.AnnotatedElementUtils
import org.springframework.core.annotation.Order
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.util.StringUtils
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.context.request.RequestAttributes
import org.springframework.web.context.request.WebRequest
import org.springframework.web.util.WebUtils
import top.bettercode.summer.tools.lang.property.Settings.exceptionHandle
import top.bettercode.summer.web.IRespEntity
import top.bettercode.summer.web.RespEntity
import top.bettercode.summer.web.properties.SummerWebProperties
import java.io.PrintWriter
import java.io.StringWriter
import java.util.*
import javax.servlet.RequestDispatcher

/**
 * ErrorAttributes 错误属性
 *
 * @author Peter Wu
 */
@Order(Ordered.HIGHEST_PRECEDENCE)
class ErrorAttributes(private val errorProperties: ErrorProperties,
                      private val errorHandlers: List<IErrorHandler>?,
                      private val errorRespEntityHandler: IErrorRespEntityHandler?,
                      private val messageSource: MessageSource, private val summerWebProperties: SummerWebProperties) : DefaultErrorAttributes() {
    override fun getErrorAttributes(webRequest: WebRequest,
                                    options: ErrorAttributeOptions): Map<String?, Any?> {
        val error = getError(webRequest)
        return getErrorAttributes(error, webRequest, options.isIncluded(ErrorAttributeOptions.Include.STACK_TRACE)).toMap()
    }

    fun getErrorAttributes(error: Throwable?, webRequest: WebRequest): IRespEntity {
        return getErrorAttributes(error, webRequest, isIncludeStackTrace(webRequest, MediaType.ALL))
    }

    fun getErrorAttributes(error: Throwable?, webRequest: WebRequest,
                           includeStackTrace: Boolean): IRespEntity {
        var statusCode: String? = null
        var httpStatusCode: Int? = null
        var message: String?
        val respEntity = RespEntity<Any>()
        val errors: MutableMap<String?, String?> = HashMap()
        if (error != null) {
            if (errorHandlers != null) {
                for (errorHandler in errorHandlers) {
                    errorHandler.handlerException(error, respEntity, errors,
                            summerWebProperties.constraintViolationSeparator)
                }
            }
            statusCode = respEntity.status
            httpStatusCode = respEntity.httpStatusCode
            message = respEntity.message
            if (includeStackTrace) {
                addStackTrace(respEntity, error)
            }
            if (!StringUtils.hasText(message)) {
                message = handleMessage(error.javaClass)
                if (StringUtils.hasText(error.message) && (!StringUtils.hasText(message) || error.message?.contains("Exception") != true)) {
                    message = error.message
                }
            }
            if (httpStatusCode == null) {
                val errorClass: Class<out Throwable> = error.javaClass
                httpStatusCode = handleHttpStatusCode(errorClass)
                val responseStatus = AnnotatedElementUtils
                        .findMergedAnnotation(errorClass, ResponseStatus::class.java)
                if (responseStatus != null) {
                    if (httpStatusCode == null) {
                        httpStatusCode = responseStatus.code.value()
                    }
                    val reason = responseStatus.reason
                    if (!StringUtils.hasText(message) && StringUtils.hasText(reason)) {
                        message = reason
                    }
                }
            }
        } else {
            message = getMessage(webRequest)
        }
        if (httpStatusCode == null) {
            httpStatusCode = getStatus(webRequest).value()
        }
        statusCode = statusCode ?: httpStatusCode.toString()
        if (!StringUtils.hasText(message)) {
            message = if (httpStatusCode == 404) {
                "resource.not.found"
            } else {
                ""
            }
        }
        message = getText(webRequest, message).trim { it <= ' ' }
        if (message.matches(".*query did not return a unique result:.*".toRegex())) {
            message = getText(webRequest, "data.not.unique.result")
        }
        setErrorInfo(webRequest, httpStatusCode, message, error)
        respEntity.status = statusCode
        respEntity.message = message
        if (!errors.isEmpty()) {
            respEntity.errors = errors
        }
        return errorRespEntityHandler?.handle(webRequest, respEntity) ?: respEntity
    }

    private fun handleHttpStatusCode(throwableClass: Class<out Throwable>): Int? {
        val key = throwableClass.name + ".code"
        val value = propertiesSource[key]
        return if (StringUtils.hasText(value)) {
            value!!.toInt()
        } else {
            null
        }
    }

    private fun handleMessage(throwableClass: Class<out Throwable>): String? {
        val key = throwableClass.name + ".message"
        return propertiesSource[key]
    }

    private fun setErrorInfo(request: WebRequest, httpStatusCode: Int,
                             message: String,
                             error: Throwable?) {
        request.setAttribute(RequestDispatcher.ERROR_STATUS_CODE, httpStatusCode,
                RequestAttributes.SCOPE_REQUEST)
        request.setAttribute(DefaultErrorAttributes::class.java.name + ".ERROR", error, RequestAttributes.SCOPE_REQUEST)
        request
                .setAttribute(WebUtils.ERROR_MESSAGE_ATTRIBUTE, message, RequestAttributes.SCOPE_REQUEST)
    }

    /**
     * 增加StackTrace
     *
     * @param respEntity respEntity
     * @param error      error
     */
    private fun addStackTrace(respEntity: RespEntity<Any>, error: Throwable) {
        val stackTrace = StringWriter()
        error.printStackTrace(PrintWriter(stackTrace))
        stackTrace.flush()
        respEntity.trace = stackTrace.toString()
    }

    private fun getStatus(requestAttributes: RequestAttributes): HttpStatus {
        val statusCode = getAttribute<Int>(requestAttributes, WebUtils.ERROR_STATUS_CODE_ATTRIBUTE)
        try {
            return statusCode?.let { HttpStatus.valueOf(it) }?:HttpStatus.INTERNAL_SERVER_ERROR
        } catch (ignored: Exception) {
        }
        return HttpStatus.INTERNAL_SERVER_ERROR
    }

    private fun getMessage(requestAttributes: RequestAttributes): String? {
        return getAttribute(requestAttributes, WebUtils.ERROR_MESSAGE_ATTRIBUTE)
    }

    private fun <T> getAttribute(requestAttributes: RequestAttributes, name: String): T? {
        return requestAttributes.getAttribute(name, RequestAttributes.SCOPE_REQUEST) as? T
    }

    /**
     * 得到国际化信息 未找到时返回代码 code
     *
     * @param webRequest webRequest
     * @param code       模板
     * @param args       参数
     * @return 信息
     */
    private fun getText(webRequest: WebRequest?, code: Any?, vararg args: Any): String {
        val codeString = code.toString()
        return messageSource.getMessage(codeString, args, codeString,
                if (webRequest == null) Locale.CHINA else webRequest.locale)?:""
    }

    /**
     * Determine if the stacktrace attribute should be included.
     *
     * @param request  the source request
     * @param produces the media type produced (or `MediaType.ALL`)
     * @return if the stacktrace attribute should be included
     */
    protected fun isIncludeStackTrace(request: WebRequest, produces: MediaType?): Boolean {
        return when (errorProperties.includeStacktrace) {
            IncludeAttribute.ALWAYS -> true
            IncludeAttribute.ON_PARAM -> getTraceParameter(request)
            else -> false
        }
    }

    private fun getTraceParameter(request: WebRequest): Boolean {
        val parameter = request.getParameter("trace") ?: return false
        return !"false".equals(parameter, ignoreCase = true)
    }

    companion object {
        private val log = LoggerFactory.getLogger(ErrorAttributes::class.java)
        val IS_PLAIN_TEXT_ERROR = ErrorAttributes::class.java.name + ".plainText"
        private val propertiesSource = exceptionHandle
    }
}
