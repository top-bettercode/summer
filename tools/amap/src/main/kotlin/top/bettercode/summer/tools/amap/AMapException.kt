package top.bettercode.summer.tools.amap

/**
 * @author Peter Wu
 */
class AMapException : RuntimeException {
    @JvmOverloads
    constructor(message: String? = "高德地图请求失败") : super(message)

    constructor(message: String?, cause: Throwable?) : super(message, cause)
    constructor(cause: Throwable?) : this("高德地图请求失败", cause)
    constructor(
        message: String?, cause: Throwable?, enableSuppression: Boolean,
        writableStackTrace: Boolean
    ) : super(message, cause, enableSuppression, writableStackTrace)

    companion object {
        private const val serialVersionUID = 1L
    }
}