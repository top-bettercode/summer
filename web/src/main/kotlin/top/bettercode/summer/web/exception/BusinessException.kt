package top.bettercode.summer.web.exception

import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.ResponseStatus

/**
 * @author Peter Wu
 */
@ResponseStatus(HttpStatus.OK)
class BusinessException : SystemException {
    constructor(httpStatusCode: Int) : super(httpStatusCode)
    constructor(httpStatusCode: Int, cause: Throwable?) : super(httpStatusCode, cause)
    constructor(httpStatusCode: Int, data: Any?) : super(httpStatusCode, data)
    constructor(code: String) : super(code)
    constructor(code: String, cause: Throwable?) : super(code, cause)
    constructor(code: String, data: Any?) : super(code, data)
    constructor(code: String, message: String?) : super(code, message)
    constructor(code: String, message: String?, cause: Throwable?) : super(code, message, cause)
    constructor(code: String, message: String?, data: Any?) : super(code, message, data)
    constructor(httpStatusCode: Int, message: String?) : super(httpStatusCode, message)
    constructor(httpStatusCode: Int, message: String?, cause: Throwable?) : super(httpStatusCode, message, cause)
    constructor(httpStatusCode: Int, message: String?, data: Any?) : super(httpStatusCode, message, data)
    constructor(httpStatusCode: Int, code: String, message: String?) : super(httpStatusCode, code, message)
    constructor(httpStatusCode: Int, code: String, message: String?, cause: Throwable?) : super(httpStatusCode, code, message, cause)
    constructor(httpStatusCode: Int, code: String, message: String?, data: Any?) : super(httpStatusCode, code, message, data)
}
