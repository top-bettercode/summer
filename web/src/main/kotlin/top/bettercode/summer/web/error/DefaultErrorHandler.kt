package top.bettercode.summer.web.error

import com.fasterxml.jackson.databind.exc.InvalidFormatException
import org.springframework.beans.InvalidPropertyException
import org.springframework.context.MessageSource
import org.springframework.core.convert.ConversionFailedException
import org.springframework.http.HttpStatus
import org.springframework.http.converter.HttpMessageNotReadableException
import org.springframework.http.converter.HttpMessageNotWritableException
import org.springframework.validation.BindException
import org.springframework.validation.FieldError
import org.springframework.web.HttpMediaTypeNotAcceptableException
import org.springframework.web.HttpRequestMethodNotSupportedException
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.UnsatisfiedServletRequestParameterException
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException
import org.springframework.web.multipart.MultipartException
import top.bettercode.summer.web.RespEntity
import top.bettercode.summer.web.exception.SystemException
import top.bettercode.summer.web.validator.NoPropertyPath
import javax.servlet.http.HttpServletRequest
import javax.validation.ConstraintViolation
import javax.validation.ConstraintViolationException

/**
 * @author Peter Wu
 */
class DefaultErrorHandler(
    messageSource: MessageSource,
    request: HttpServletRequest?
) : AbstractErrorHandler(messageSource, request) {
    override fun handlerException(
        error: Throwable, respEntity: RespEntity<*>,
        errors: MutableMap<String?, String?>, separator: String
    ) {
        if (error is IllegalArgumentException) {
            val regex = "Parameter specified as non-null is null: .*parameter (.*)"
            if (error.message?.matches(regex.toRegex()) == true) {
                val paramName = error.message!!.replace(regex.toRegex(), "$1")
                respEntity.message = getText(paramName) + getText("canTBeEmpty")
            }
        } else if (error is HttpRequestMethodNotSupportedException) {
            respEntity.message = "method.not.allowed"
        } else if (error is MethodArgumentNotValidException) {
            val bindingResult = error.bindingResult
            val fieldErrors = bindingResult.fieldErrors
            respEntity.message = handleFieldError(errors, fieldErrors, separator)
        } else if (error is BindException) { //参数错误
            val fieldErrors = error.fieldErrors
            respEntity.message = handleFieldError(errors, fieldErrors, separator)
        } else if (error is MethodArgumentTypeMismatchException) {
            val argumentName = error.name
            val targetType = error.requiredType?.name
            var message: String?
            if (targetType != null) {
                val code = "typeMismatch.$targetType"
                message = getText("typeMismatch.$targetType")
                if (message == code) {
                    message = getText("typeMismatch.type", targetType)
                }
            } else {
                message = getText("typeMismatch")
            }
            respEntity.message =
                getText(argumentName) + separator + invalidValue(error.value) + message
            errors[argumentName] = message
        } else if (error is ConversionFailedException) {
            val targetType = error.targetType.type.name
            val code = "typeMismatch.$targetType"
            var message = getText(code)
            if (message == code)
                message = getText("typeMismatch.type", targetType)
            respEntity.message = invalidValue(error.value) + message
        } else if (error is ConstraintViolationException) { //数据验证
            constraintViolationException(
                error, respEntity, errors,
                separator
            )
        } else if (error is HttpMessageNotReadableException) {
            val cause = error.cause
            if (cause is InvalidFormatException) {
                val targetType = cause.targetType.name
                val code = "typeMismatch.$targetType"
                var message = getText(code)
                if (message == code)
                    message = getText("typeMismatch.type", targetType)
                val path = cause.path
                val desc =
                    path.joinToString("") { if (it.fieldName == null) "[${it.index}]" else ".${it.fieldName}" }
                        .trimStart('.')
                errors[desc] = getText(
                    path.last().fieldName
                        ?: desc
                ) + separator + invalidValue(cause.value) + message
                respEntity.message = errors.values.first()
            } else {
                respEntity.message = "paramMismatch"
            }
        } else if (error is HttpMediaTypeNotAcceptableException) {
            respEntity.message = "MediaType not Acceptable!Must ACCEPT:" + error
                .supportedMediaTypes
        } else if (error is HttpMessageNotWritableException) {
            if (error.message != null && error.message!!.contains("Session is closed")) {
                respEntity.httpStatusCode = HttpStatus.REQUEST_TIMEOUT.value()
                respEntity.message = "request.timeout"
            }
        } else if (error is UnsatisfiedServletRequestParameterException) {
            val paramConditionGroups = error.paramConditionGroups
            val sb = StringBuilder("${getText("UnsatisfiedParam")}，需要参数：")
            val paramNames = mutableListOf<String>()
            for ((i, conditions) in paramConditionGroups.withIndex()) {
                if (i > 0) {
                    sb.append(" OR ")
                }
                conditions.forEach { c ->
                    paramNames.add(c.substringBefore("="))
                }
                sb.append(conditions.joinToString())
            }
            sb.append("，实际参数：")
            var i = 0
            for (v in error.actualParams.entries) {
                if (paramNames.contains(v.key)) {
                    if (i > 0) {
                        sb.append("&")
                    }
                    sb.append("${v.key}=${v.value.joinToString()}")
                    i++
                }
            }
            respEntity.message = sb.toString()
        } else if (error is NullPointerException) {
            respEntity.message = "data.exception"
        } else if (error is MultipartException) {
            respEntity.message = "upload.fail"
        } else if (error is InvalidPropertyException) {
            respEntity.message = "paramMismatch"
        } else if (error is SystemException) {
            respEntity.httpStatusCode = error.httpStatusCode
            respEntity.status = error.code
            respEntity.message = error.message
            respEntity.errors = error.data
        }
    }

    private fun handleFieldError(
        errors: MutableMap<String?, String?>,
        fieldErrors: List<FieldError>, separator: String
    ): String {
        for (fieldError in fieldErrors) {
            var defaultMessage = fieldError.defaultMessage
            if (defaultMessage?.contains("required type") == true && fieldError.codes != null) {
                for (code in fieldError.codes!!) {
                    val text = getText(code)
                    if (text != code) {
                        defaultMessage = text
                        break
                    }
                }
            }
            val regrex = "^.*threw exception; nested exception is .*: (.*)$"
            if (defaultMessage!!.matches(regrex.toRegex())) {
                defaultMessage = defaultMessage.replace(regrex.toRegex(), "$1")
                defaultMessage = getText(defaultMessage)
            }
            val field = fieldError.field
            val rejectedValue = fieldError.rejectedValue
            val rejectedValuedesc = invalidValue(rejectedValue)
            var msg: String? = null
            if (fieldError.contains(ConstraintViolation::class.java)) {
                val violation = fieldError.unwrap(ConstraintViolation::class.java)
                if (violation.constraintDescriptor.payload.contains(NoPropertyPath::class.java)) {
                    msg = violation.message
                }
            }
            if (msg == null) {
                msg =
                    getText(field.substringAfter(".")) + separator + rejectedValuedesc + defaultMessage
            }
            errors[field] = msg
        }
        var message = errors.values.joinToString()
        if (message.isBlank()) {
            message = "data.valid.failed"
        }
        return message
    }
}
