package top.bettercode.summer.tools.sap.connection

/**
 * @author Peter Wu
 */
class SapException : RuntimeException {
    constructor(message: String) : super("SAP系统：$message")
    constructor(cause: Throwable) : super(if (cause is SapException || cause is SapSysException) cause.message else "SAP系统：" + cause.message, cause)
    constructor(message: String?, cause: Throwable?) : super("SAP系统：$message", cause)

    companion object {
        private const val serialVersionUID = 1L
    }
}
