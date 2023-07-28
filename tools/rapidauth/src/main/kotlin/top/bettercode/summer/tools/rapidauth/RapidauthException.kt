package top.bettercode.summer.tools.rapidauth

/**
 * @author Peter Wu
 */
class RapidauthException : RuntimeException {
    @JvmOverloads
    constructor(message: String? = "腾讯云号码认证请求失败") : super(message)

    constructor(message: String?, cause: Throwable?) : super(message, cause)
    constructor(cause: Throwable?) : this("腾讯云号码认证请求失败", cause)
    constructor(
            message: String?, cause: Throwable?, enableSuppression: Boolean,
            writableStackTrace: Boolean
    ) : super(message, cause, enableSuppression, writableStackTrace)

    companion object {
        private const val serialVersionUID = 1L
    }
}