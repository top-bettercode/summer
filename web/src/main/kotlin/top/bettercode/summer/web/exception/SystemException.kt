package top.bettercode.summer.web.exception

import top.bettercode.summer.tools.lang.property.PropertiesSource.Companion.of

/**
 * @author Peter Wu
 */
open class SystemException : RuntimeException {
    /**
     * 业务错误码
     */
    val code: String
    val data: Any?

    constructor(code: String) : super(propertiesSource.getOrDefault(code, code)) {
        this.code = code
        data = null
    }

    constructor(code: String, cause: Throwable?) : super(propertiesSource.getOrDefault(code, code), cause) {
        this.code = code
        data = null
    }

    constructor(code: String, data: Any?) : super(propertiesSource.getOrDefault(code, code)) {
        this.code = code
        this.data = data
    }

    constructor(code: String, message: String?) : super(message) {
        this.code = code
        data = null
    }

    constructor(code: String, message: String?, cause: Throwable?) : super(message, cause) {
        this.code = code
        data = null
    }

    constructor(code: String, message: String?, data: Any?) : super(message) {
        this.code = code
        this.data = data
    }

    companion object {
        private val propertiesSource = of("error-code", "properties.error-code")
    }
}
