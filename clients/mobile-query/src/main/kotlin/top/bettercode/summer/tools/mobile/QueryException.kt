package top.bettercode.summer.tools.mobile

/**
 * @author Peter Wu
 */
class QueryException : RuntimeException {
    @JvmOverloads
    constructor(message: String? = "查询平台请求失败") : super(message)

    constructor(message: String?, cause: Throwable?) : super(message, cause)
    constructor(cause: Throwable?) : this("查询平台请求失败", cause)
    constructor(
            message: String?, cause: Throwable?, enableSuppression: Boolean,
            writableStackTrace: Boolean
    ) : super(message, cause, enableSuppression, writableStackTrace)

}