package top.bettercode.summer.tools.excel

/**
 * @author Peter Wu
 */
class ExcelException : RuntimeException {
    constructor(message: String?) : super(message)
    constructor(message: String?, cause: Throwable?) : super(message, cause)

}
