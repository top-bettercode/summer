package top.bettercode.summer.tools.sms

/**
 * @author Peter Wu
 */
class SmsException : RuntimeException {
    @JvmOverloads
    constructor(message: String? = "短信平台请求失败") : super(message)
    constructor(message: String?, cause: Throwable?) : super(message, cause)
    constructor(cause: Throwable?) : this("短信平台请求失败", cause)
    constructor(message: String?, cause: Throwable?, enableSuppression: Boolean,
                writableStackTrace: Boolean) : super(message, cause, enableSuppression, writableStackTrace)

    companion object
}
