package top.bettercode.summer.web.error

import org.springframework.context.MessageSource
import org.springframework.core.convert.ConversionFailedException
import org.springframework.http.HttpStatus
import org.springframework.http.converter.HttpMessageNotWritableException
import org.springframework.util.StringUtils
import org.springframework.validation.BindException
import org.springframework.validation.FieldError
import org.springframework.web.HttpMediaTypeNotAcceptableException
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.UnsatisfiedServletRequestParameterException
import top.bettercode.summer.web.RespEntity
import top.bettercode.summer.web.exception.BusinessException
import top.bettercode.summer.web.exception.SystemException
import top.bettercode.summer.web.validator.NoPropertyPath
import java.util.*
import javax.servlet.http.HttpServletRequest
import javax.validation.ConstraintViolation
import javax.validation.ConstraintViolationException

/**
 * @author Peter Wu
 */
class DefaultErrorHandler(messageSource: MessageSource,
                          request: HttpServletRequest?) : AbstractErrorHandler(messageSource, request) {
    override fun handlerException(error: Throwable, respEntity: RespEntity<*>,
                                  errors: MutableMap<String?, String?>, separator: String) {
        var message: String? = null
        if (error is IllegalArgumentException) {
            val regex = "Parameter specified as non-null is null: .*parameter (.*)"
            if (error.message!!.matches(regex.toRegex())) {
                val paramName = error.message!!.replace(regex.toRegex(), "$1")
                message = getText(paramName) + "不能为空"
            }
        } else if (error is MethodArgumentNotValidException) {
            val bindingResult = error.bindingResult
            val fieldErrors = bindingResult.fieldErrors
            message = handleFieldError(errors, fieldErrors, separator)
        } else if (error is BindException) { //参数错误
            val fieldErrors = error.fieldErrors
            message = handleFieldError(errors, fieldErrors, separator)
        } else if (error is ConversionFailedException) {
            message = getText("typeMismatch",
                    error.value,
                    error.targetType)
        } else if (error is ConstraintViolationException) { //数据验证
            constraintViolationException(error, respEntity, errors,
                    separator)
        } else if (error is HttpMediaTypeNotAcceptableException) {
            message = "MediaType not Acceptable!Must ACCEPT:" + error
                    .supportedMediaTypes
        } else if (error is HttpMessageNotWritableException) {
            message = error.message
            if (message != null && message.contains("Session is closed")) {
                respEntity.setHttpStatusCode(HttpStatus.REQUEST_TIMEOUT.value())
                message = "request.timeout"
            }
        } else if (error is UnsatisfiedServletRequestParameterException) {
            val paramConditionGroups = error.paramConditionGroups
            val sb = StringBuilder("参数不匹配:")
            for ((i, conditions) in paramConditionGroups.withIndex()) {
                if (i > 0) {
                    sb.append(" OR ")
                }
                sb.append('"')
                sb.append(StringUtils.arrayToDelimitedString(conditions, ", "))
                sb.append('"')
            }
            message = sb.toString()
        } else if (error is BusinessException) {
            respEntity.status = error.code
            respEntity.errors = error.data
        } else if (error is SystemException) {
            val code = error.code
            try {
                respEntity.setHttpStatusCode(code.toInt())
            } catch (e: NumberFormatException) {
                respEntity.status = code
            }
            respEntity.errors = error.data
        }
        if (StringUtils.hasText(message)) {
            respEntity.message = message
        }
    }

    private fun handleFieldError(errors: MutableMap<String?, String?>,
                                 fieldErrors: List<FieldError>, separator: String): String? {
        var message: String?
        for (fieldError in fieldErrors) {
            var defaultMessage = fieldError.defaultMessage
            if (Objects.requireNonNull(defaultMessage).contains("required type")) {
                defaultMessage = fieldError.code?.let { getText(it) }
            }
            val regrex = "^.*threw exception; nested exception is .*: (.*)$"
            if (defaultMessage!!.matches(regrex.toRegex())) {
                defaultMessage = defaultMessage.replace(regrex.toRegex(), "$1")
                defaultMessage = getText(defaultMessage)
            }
            val field = fieldError.field
            var msg: String? = null
            if (fieldError.contains(ConstraintViolation::class.java)) {
                val violation = fieldError.unwrap(ConstraintViolation::class.java)
                if (violation.constraintDescriptor.payload.contains(NoPropertyPath::class.java)) {
                    msg = violation.message
                }
            }
            if (msg == null) {
                msg = if (field.contains(".")) {
                    (getText(field.substring(field.lastIndexOf('.') + 1)) + separator
                            + defaultMessage)
                } else {
                    getText(field) + separator + defaultMessage
                }
            }
            errors[field] = msg
        }
        message = errors.values.iterator().next()
        if (!StringUtils.hasText(message)) {
            message = "data.valid.failed"
        }
        return message
    }
}
