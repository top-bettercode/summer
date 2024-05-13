package top.bettercode.summer.security.support

import org.springframework.context.MessageSource
import org.springframework.http.HttpStatus
import org.springframework.security.authentication.BadCredentialsException
import top.bettercode.summer.web.RespEntity
import top.bettercode.summer.web.error.AbstractErrorHandler
import javax.servlet.http.HttpServletRequest

/**
 * @author Peter Wu
 */
class ApiSecurityErrorHandler(
    messageSource: MessageSource,
    request: HttpServletRequest?
) : AbstractErrorHandler(messageSource, request) {
    override fun handlerException(
        error: Throwable, respEntity: RespEntity<*>,
        errors: MutableMap<String?, String?>, separator: String
    ) {
        if (error is IllegalUserException) {
            respEntity.httpStatusCode = HttpStatus.BAD_REQUEST.value()
            val userErrors = error.errors
            if (!userErrors.isNullOrEmpty()) {
                errors.putAll(userErrors)
            }
        } else if (error is BadCredentialsException) {
            respEntity.httpStatusCode = HttpStatus.BAD_REQUEST.value()
        }
    }
}
