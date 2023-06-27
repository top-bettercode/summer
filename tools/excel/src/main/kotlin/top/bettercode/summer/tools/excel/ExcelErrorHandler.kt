package top.bettercode.summer.tools.excel

import org.springframework.context.MessageSource
import org.springframework.util.StringUtils
import top.bettercode.summer.web.RespEntity
import top.bettercode.summer.web.error.AbstractErrorHandler
import java.time.format.DateTimeParseException
import javax.servlet.http.HttpServletRequest
import javax.validation.ConstraintViolationException
import kotlin.collections.MutableMap
import kotlin.collections.component1
import kotlin.collections.component2
import kotlin.collections.set

/**
 * @author Peter Wu
 */
class ExcelErrorHandler(messageSource: MessageSource?,
                        request: HttpServletRequest?) : AbstractErrorHandler(messageSource!!, request) {
    override fun handlerException(error: Throwable, respEntity: RespEntity<*>,
                                  errors: MutableMap<String?, String?>, separator: String) {
        var message: String? = null
        if (error is ExcelImportException) {
            val cellErrors = error.errors
            for (cellError in cellErrors) {
                val key = getText(cellError.message, cellError.row,
                        cellError.columnName)
                val title = cellError.title
                val exception = cellError.exception
                val value = cellError.value
                if (exception is ConstraintViolationException) {
                    for (constraintViolation in exception
                            .constraintViolations) {
                        errors[key] = title + ": [" + value + "]" + constraintViolation.message
                    }
                } else {
                    var msg = exception.message
                    if (exception is DateTimeParseException) {
                        val msgRegex = "Text '(.*?)' could not be parsed at index (\\d+)"
                        if (msg!!.matches(msgRegex.toRegex())) {
                            msg = msg.replace(msgRegex.toRegex(), "$1") + "不是有效的日期格式"
                        }
                    }
                    errors[key] = title + ": [" + value + "]" + getText(msg!!)
                }
            }
            val (key, value) = errors.entries.iterator().next()
            message = key + separator + value
        }
        if (StringUtils.hasText(message)) {
            respEntity.message = message
        }
    }
}
