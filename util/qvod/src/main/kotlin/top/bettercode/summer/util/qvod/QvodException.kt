package top.bettercode.summer.util.qvod

/**
 * @author Peter Wu
 */
class QvodException : RuntimeException {
    @JvmOverloads
    constructor(message: String? = "天气数据平台请求失败") : super(message) {
    }

    constructor(message: String?, cause: Throwable?) : super(message, cause) {}
    constructor(cause: Throwable?) : this("天气数据平台请求失败", cause) {}
    constructor(
        message: String?, cause: Throwable?, enableSuppression: Boolean,
        writableStackTrace: Boolean
    ) : super(message, cause, enableSuppression, writableStackTrace) {
    }

    companion object {
        private const val serialVersionUID = 1L
    }
}