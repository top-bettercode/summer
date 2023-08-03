package top.bettercode.summer.tools.jpush

/**
 * @author Peter Wu
 */
class JpushException : RuntimeException {
    @JvmOverloads
    constructor(message: String? = "极光推送平台请求失败") : super(message)

    constructor(message: String?, cause: Throwable?) : super(message, cause)
    constructor(cause: Throwable?) : this("极光推送平台请求失败", cause)
    constructor(
            message: String?, cause: Throwable?, enableSuppression: Boolean,
            writableStackTrace: Boolean
    ) : super(message, cause, enableSuppression, writableStackTrace)

    companion object
}