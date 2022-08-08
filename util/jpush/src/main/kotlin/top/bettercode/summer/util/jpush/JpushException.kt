package top.bettercode.summer.util.jpush

import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.ResponseStatus

/**
 * @author Peter Wu
 */
@ResponseStatus(HttpStatus.BAD_REQUEST)
class JpushException : RuntimeException {
    @JvmOverloads
    constructor(message: String? = "极光推送平台请求失败") : super(message) {
    }

    constructor(message: String?, cause: Throwable?) : super(message, cause) {}
    constructor(cause: Throwable?) : this("极光推送平台请求失败", cause) {}
    constructor(
        message: String?, cause: Throwable?, enableSuppression: Boolean,
        writableStackTrace: Boolean
    ) : super(message, cause, enableSuppression, writableStackTrace) {
    }

    companion object {
        private const val serialVersionUID = 1L
    }
}