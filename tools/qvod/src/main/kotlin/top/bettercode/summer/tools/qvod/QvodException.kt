package top.bettercode.summer.tools.qvod

/**
 * @author Peter Wu
 */
class QvodException : RuntimeException {
    @JvmOverloads
    constructor(message: String? = "腾讯云点播请求失败") : super(message)

    constructor(message: String?, cause: Throwable?) : super(message, cause)
    constructor(cause: Throwable?) : this("腾讯云点播请求失败", cause)
    constructor(
            message: String?, cause: Throwable?, enableSuppression: Boolean,
            writableStackTrace: Boolean
    ) : super(message, cause, enableSuppression, writableStackTrace)

    companion object
}