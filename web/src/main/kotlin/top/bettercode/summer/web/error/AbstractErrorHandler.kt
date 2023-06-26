package top.bettercode.summer.web.error

import org.hibernate.validator.internal.engine.path.PathImpl
import org.springframework.context.MessageSource
import org.springframework.http.HttpStatus
import org.springframework.util.StringUtils
import top.bettercode.summer.web.RespEntity
import top.bettercode.summer.web.validator.NoPropertyPath
import java.util.*
import javax.servlet.http.HttpServletRequest
import javax.validation.ConstraintViolation
import javax.validation.ConstraintViolationException

/**
 * @author Peter Wu
 */
abstract class AbstractErrorHandler(private val messageSource: MessageSource,
                                    private val request: HttpServletRequest?) : IErrorHandler {
    override fun getText(code: Any, vararg args: Any?): String {
        val codeString = code.toString()
        return messageSource.getMessage(codeString, args, codeString,
                if (request == null) Locale.CHINA else request.locale) ?: ""
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

    protected fun constraintViolationException(error: ConstraintViolationException,
                                               respEntity: RespEntity<*>, errors: MutableMap<String?, String?>,
                                               separator: String) {
        var message: String?
        respEntity.setHttpStatusCode(HttpStatus.UNPROCESSABLE_ENTITY.value())
        val constraintViolations = error.constraintViolations
        for (constraintViolation in constraintViolations) {
            val property = getProperty(constraintViolation)
            val invalidValue = "(${constraintViolation.invalidValue})"
            val msg: String? = if (constraintViolation.constraintDescriptor.payload
                            .contains(NoPropertyPath::class.java)) {
                constraintViolation.message
            } else {
                getText(property) + invalidValue + separator + constraintViolation.message
            }
            errors[property] = msg
        }
        message = errors.values.iterator().next()
        if (!StringUtils.hasText(message)) {
            message = "data.valid.failed"
        }
        respEntity.message = message
    }
}
