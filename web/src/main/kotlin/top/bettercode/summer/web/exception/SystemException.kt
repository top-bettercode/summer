package top.bettercode.summer.web.exception

import top.bettercode.summer.tools.lang.property.PropertiesSource.Companion.of

/**
 * @author Peter Wu
 */
open class SystemException : RuntimeException {
    /**
     * 业务错误码
     */
    val httpStatusCode: Int?
    val code: String
    val data: Any?

    constructor(httpStatusCode: Int) : super(propertiesSource.getOrDefault(httpStatusCode.toString(), httpStatusCode.toString())) {
        this.httpStatusCode = httpStatusCode
        this.code = httpStatusCode.toString()
        data = null
    }

    constructor(httpStatusCode: Int, cause: Throwable?) : super(propertiesSource.getOrDefault(httpStatusCode.toString(), httpStatusCode.toString()), cause) {
        this.httpStatusCode = httpStatusCode
        this.code = httpStatusCode.toString()
        data = null
    }

    constructor(httpStatusCode: Int, data: Any?) : super(propertiesSource.getOrDefault(httpStatusCode.toString(), httpStatusCode.toString())) {
        this.httpStatusCode = httpStatusCode
        this.code = httpStatusCode.toString()
        this.data = data
    }

    constructor(code: String) : super(propertiesSource.getOrDefault(code, code)) {
        this.httpStatusCode = null
        this.code = code
        data = null
    }

    constructor(code: String, cause: Throwable?) : super(propertiesSource.getOrDefault(code, code), cause) {
        this.httpStatusCode = null
        this.code = code
        data = null
    }

    constructor(code: String, data: Any?) : super(propertiesSource.getOrDefault(code, code)) {
        this.httpStatusCode = null
        this.code = code
        this.data = data
    }

    constructor(code: String, message: String?) : super(message) {
        this.httpStatusCode = null
        this.code = code
        data = null
    }

    constructor(code: String, message: String?, cause: Throwable?) : super(message, cause) {
        this.httpStatusCode = null
        this.code = code
        data = null
    }

    constructor(code: String, message: String?, data: Any?) : super(message) {
        this.httpStatusCode = null
        this.code = code
        this.data = data
    }

    constructor(httpStatusCode: Int, message: String?) : super(message) {
        this.httpStatusCode = httpStatusCode
        this.code = httpStatusCode.toString()
        data = null
    }

    constructor(httpStatusCode: Int, message: String?, cause: Throwable?) : super(message, cause) {
        this.httpStatusCode = httpStatusCode
        this.code = httpStatusCode.toString()
        data = null
    }

    constructor(httpStatusCode: Int, message: String?, data: Any?) : super(message) {
        this.httpStatusCode = httpStatusCode
        this.code = httpStatusCode.toString()
        this.data = data
    }

    constructor(httpStatusCode: Int, code: String, message: String?) : super(message) {
        this.httpStatusCode = httpStatusCode
        this.code = code
        data = null
    }

    constructor(httpStatusCode: Int, code: String, message: String?, cause: Throwable?) : super(message, cause) {
        this.httpStatusCode = httpStatusCode
        this.code = code
        data = null
    }

    constructor(httpStatusCode: Int, code: String, message: String?, data: Any?) : super(message) {
        this.httpStatusCode = httpStatusCode
        this.code = code
        this.data = data
    }

    companion object {
        private val propertiesSource = of("error-code", "properties.error-code")
    }
}
