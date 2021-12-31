package top.bettercode.api.sign

import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.ResponseStatus

/**
 * @author Peter Wu
 */
@ResponseStatus(HttpStatus.NOT_ACCEPTABLE, reason = "ILLEGAL_SIGN")
class IllegalSignException : RuntimeException("ILLEGAL_SIGN") {
    companion object {
        private const val serialVersionUID: Long = 1L
    }
}
