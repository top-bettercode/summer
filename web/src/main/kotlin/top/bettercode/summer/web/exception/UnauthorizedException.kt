package top.bettercode.summer.web.exception

import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.ResponseStatus

/**
 * @author Peter Wu
 */
@ResponseStatus(value = HttpStatus.UNAUTHORIZED, reason = "Unauthorized")
class UnauthorizedException : RuntimeException {
    constructor()
    constructor(message: String?) : super(message)
    constructor(message: String?, cause: Throwable?) : super(message, cause)

    companion object {
        private const val serialVersionUID = 1L
    }
}
