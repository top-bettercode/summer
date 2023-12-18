package top.bettercode.summer.web.error

import jakarta.servlet.http.HttpServletRequest
import jakarta.validation.ConstraintViolation
import jakarta.validation.ConstraintViolationException
import org.hibernate.validator.internal.engine.path.PathImpl
import org.springframework.context.MessageSource
import org.springframework.http.HttpStatus
import top.bettercode.summer.tools.lang.util.StringUtil
import top.bettercode.summer.web.RespEntity
import top.bettercode.summer.web.validator.NoPropertyPath
import java.util.*

/**
 * @author Peter Wu
 */
abstract class AbstractErrorHandler(
    private val messageSource: MessageSource,
    private val request: HttpServletRequest?
) : IErrorHandler {
    override fun getText(code: Any, vararg args: Any?): String {
        val codeString = code.toString()
        return messageSource.getMessage(
            codeString, args, codeString,
            if (request == null) Locale.CHINA else request.locale
        ) ?: ""
    }

    fun getProperty(constraintViolation: ConstraintViolation<*>): String {
        val propertyPath = constraintViolation.propertyPath
        var property = propertyPath.toString()
        if (propertyPath is PathImpl) {
            property = propertyPath.leafNode.name
        }
        if (property.contains(".")) {
            property = property.substring(property.lastIndexOf('.') + 1)
        }
        return property
    }

    protected fun constraintViolationException(
        error: ConstraintViolationException,
        respEntity: RespEntity<*>, errors: MutableMap<String?, String?>,
        separator: String
    ) {
        respEntity.httpStatusCode = HttpStatus.UNPROCESSABLE_ENTITY.value()
        val constraintViolations = error.constraintViolations
        for (constraintViolation in constraintViolations) {
            val property = getProperty(constraintViolation)
            val invalidValue = constraintViolation.invalidValue
            val invalidValueDesc = invalidValue(invalidValue)
            val msg: String = if (constraintViolation.constraintDescriptor.payload
                    .contains(NoPropertyPath::class.java)
            ) {
                invalidValueDesc + getText((constraintViolation.message ?: "data.valid.failed"))
            } else {
                getText(property) + separator + invalidValueDesc + getText(constraintViolation.message)
            }
            errors[property] = msg
        }
        var message = errors.values.joinToString()
        if (message.isBlank()) {
            message = "data.valid.failed"
        }
        respEntity.message = message
    }

    companion object {
        fun invalidValue(invalidValue: Any?): String {
            return when {
                invalidValue == null
                        || invalidValue is String && invalidValue.isBlank()
                        || invalidValue is Array<*> && invalidValue.isEmpty()
                        || invalidValue is Collection<*> && invalidValue.isEmpty() -> {
                    ""
                }

                else -> "[${StringUtil.subStringWithEllipsis("$invalidValue", 20)}]"
            }
        }
    }
}
