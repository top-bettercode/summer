package top.bettercode.summer.tools.excel

import jakarta.servlet.http.HttpServletRequest
import jakarta.validation.ConstraintViolationException
import org.springframework.context.MessageSource
import top.bettercode.summer.tools.excel.read.ExcelReaderException
import top.bettercode.summer.web.RespEntity
import top.bettercode.summer.web.error.AbstractErrorHandler
import java.time.format.DateTimeParseException
import kotlin.collections.set

/**
 * @author Peter Wu
 */
class ExcelErrorHandler(messageSource: MessageSource,
                        request: HttpServletRequest?) : AbstractErrorHandler(messageSource, request) {
    override fun handlerException(error: Throwable, respEntity: RespEntity<*>,
                                  errors: MutableMap<String?, String?>, separator: String) {
        if (error is ExcelReaderException) {
            val cellErrors = error.errors
            for (cellError in cellErrors) {
                val key = cellError.columnName + cellError.row
                val title = cellError.title
                val exception = cellError.exception
                val value = invalidValue(cellError.value)
                if (exception is ConstraintViolationException) {
                    for (constraintViolation in exception
                            .constraintViolations) {
                        errors[key] = title + ": " + value + constraintViolation.message
                    }
                } else {
                    var msg = exception.message
                    if (exception is DateTimeParseException) {
                        val msgRegex = "Text '(.*?)' could not be parsed at index (\\d+)"
                        if (msg!!.matches(msgRegex.toRegex())) {
                            msg = msg.replace(msgRegex.toRegex(), "$1") + getText("notAValidDateFormat")
                        }
                    }
                    errors[key] = title + ": " + value + getText(msg ?: "unknownError")
                }
            }
            respEntity.message = errors.entries.joinToString {
                it.key + separator + it.value
            }
        }
    }
}
