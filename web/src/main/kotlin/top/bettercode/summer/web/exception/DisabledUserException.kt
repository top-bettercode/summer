package top.bettercode.summer.web.exception

import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.ResponseStatus

/**
 * 找不到资源
 *
 * @author Peter Wu
 */
@ResponseStatus(value = HttpStatus.UNAUTHORIZED, reason = "disabledUser")
class DisabledUserException @JvmOverloads constructor(message: String? = "disabledUser", cause: Throwable? = null) : RuntimeException(message, cause) {
}