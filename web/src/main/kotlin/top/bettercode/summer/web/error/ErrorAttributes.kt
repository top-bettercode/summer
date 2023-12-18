package top.bettercode.summer.web.error

import jakarta.servlet.RequestDispatcher
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
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.context.request.RequestAttributes
import org.springframework.web.context.request.WebRequest
import org.springframework.web.util.WebUtils
import top.bettercode.summer.tools.lang.property.Settings.exceptionHandle
import top.bettercode.summer.web.IRespEntity
import top.bettercode.summer.web.RespEntity
import top.bettercode.summer.web.properties.SummerWebProperties
import java.util.*

/**
 * ErrorAttributes 错误属性
 *
 * @author Peter Wu
 */
@Order(Ordered.HIGHEST_PRECEDENCE)
open class ErrorAttributes(private val errorProperties: ErrorProperties,
                           private val errorHandlers: List<IErrorHandler>?,
                           private val respEntityConverter: IRespEntityConverter?,
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
        val respEntity = RespEntity<Any?>()
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

            if (message.isNullOrBlank()) {
                message = error.message
            }

            if (includeStackTrace) {
                respEntity.trace = error.stackTraceToString()
            }

            val errorClass: Class<out Throwable> = error.javaClass
            if (httpStatusCode == null) {
                httpStatusCode = handleHttpStatusCode(errorClass)
            }

            if (httpStatusCode == null || message.isNullOrBlank()) {
                val responseStatus = AnnotatedElementUtils
                        .findMergedAnnotation(errorClass, ResponseStatus::class.java)
                if (responseStatus != null) {
                    if (httpStatusCode == null) {
                        httpStatusCode = responseStatus.code.value()
                    }
                    if (message.isNullOrBlank()) {
                        message = responseStatus.reason
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

        if (message.isNullOrBlank()) {
            message = if (httpStatusCode == 404) {
                "resource.not.found"
            } else {
                ""
            }
        }

        message = getText(webRequest, message).trim { it <= ' ' }

        setErrorInfo(webRequest, httpStatusCode, message, error)

        respEntity.httpStatusCode = httpStatusCode
        respEntity.status = statusCode
        respEntity.message = message
        if (errors.isNotEmpty()) {
            respEntity.errors = errors
        }

        return respEntityConverter?.convert(respEntity) ?: respEntity
    }

    private fun handleHttpStatusCode(throwableClass: Class<out Throwable>): Int? {
        val key = throwableClass.name + ".code"
        val value = exceptionHandle[key]
        return if (!value.isNullOrBlank()) {
            value.toInt()
        } else {
            null
        }
    }

    private fun setErrorInfo(request: WebRequest, httpStatusCode: Int,
                             message: String,
                             error: Throwable?) {
        request.setAttribute(RequestDispatcher.ERROR_STATUS_CODE, httpStatusCode,
                RequestAttributes.SCOPE_REQUEST)
        error?.let { request.setAttribute(DefaultErrorAttributes::class.java.name + ".ERROR", it, RequestAttributes.SCOPE_REQUEST) }
        request
                .setAttribute(WebUtils.ERROR_MESSAGE_ATTRIBUTE, message, RequestAttributes.SCOPE_REQUEST)
        request.setAttribute(ERROR_ATTRIBUTES_HANDLED, true, RequestAttributes.SCOPE_REQUEST)
    }

    private fun getStatus(requestAttributes: RequestAttributes): HttpStatus {
        val statusCode = getAttribute<Int>(requestAttributes, WebUtils.ERROR_STATUS_CODE_ATTRIBUTE)
        try {
            return statusCode?.let {
                try {
                    HttpStatus.valueOf(it)
                } catch (e: Exception) {
                    null
                }
            } ?: HttpStatus.INTERNAL_SERVER_ERROR
        } catch (ignored: Exception) {
        }
        return HttpStatus.INTERNAL_SERVER_ERROR
    }

    private fun getMessage(requestAttributes: RequestAttributes): String? {
        return getAttribute(requestAttributes, WebUtils.ERROR_MESSAGE_ATTRIBUTE)
    }

    private fun <T> getAttribute(requestAttributes: RequestAttributes, name: String): T? {
        @Suppress("UNCHECKED_CAST")
        return requestAttributes.getAttribute(name, RequestAttributes.SCOPE_REQUEST) as T?
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
                if (webRequest == null) Locale.CHINA else webRequest.locale) ?: ""
    }

    /**
     * Determine if the stacktrace attribute should be included.
     *
     * @param request  the source request
     * @param produces the media type produced (or `MediaType.ALL`)
     * @return if the stacktrace attribute should be included
     */
    protected fun isIncludeStackTrace(request: WebRequest, @Suppress("UNUSED_PARAMETER") produces: MediaType?): Boolean {
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
        val IS_PLAIN_TEXT_ERROR = ErrorAttributes::class.java.name + ".plainText"
        val ERROR_ATTRIBUTES_HANDLED = ErrorAttributes::class.java.name + ".handled"
    }
}
