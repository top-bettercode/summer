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
    constructor(httpStatusCode: Int, code: String) : super(httpStatusCode, code)
    constructor(httpStatusCode: Int, code: String, cause: Throwable?) : super(httpStatusCode, code, cause)
    constructor(httpStatusCode: Int, code: String, data: Any?) : super(httpStatusCode, code, data)
    constructor(httpStatusCode: Int, code: String, message: String?) : super(httpStatusCode, code, message)
    constructor(httpStatusCode: Int, code: String, message: String?, cause: Throwable?) : super(httpStatusCode, code, message, cause)
    constructor(httpStatusCode: Int, code: String, message: String?, data: Any?) : super(httpStatusCode, code, message, data)


}
