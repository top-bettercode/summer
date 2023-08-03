package top.bettercode.summer.web.exception

import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.ResponseStatus

/**
 * @author Peter Wu
 */
@ResponseStatus(HttpStatus.OK)
class BusinessException : SystemException {
    constructor(code: String) : super(code)
    constructor(code: String, cause: Throwable?) : super(code, cause)
    constructor(code: String, data: Any?) : super(code, data)
    constructor(code: String, message: String?) : super(code, message)
    constructor(code: String, message: String?, cause: Throwable?) : super(code, message, cause)
    constructor(code: String, message: String?, data: Any?) : super(code, message, data)

}
