package top.bettercode.summer.security.support

import org.springframework.http.HttpStatus
import org.springframework.security.authentication.InternalAuthenticationServiceException
import org.springframework.web.bind.annotation.ResponseStatus

/**
 * @author Peter Wu
 */
@ResponseStatus(HttpStatus.BAD_REQUEST)
class IllegalUserException : InternalAuthenticationServiceException {
    var errors: Map<String, String>? = null

    constructor(msg: String?) : super(msg)
    constructor(msg: String?, t: Throwable?) : super(msg, t)

}
