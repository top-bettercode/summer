package cn.bestwu.logging


data class RequestLoggingConfig(
    val includeRequestBody: Boolean,
    val includeResponseBody: Boolean,
    val includeTrace: Boolean,
    val encryptHeaders: Array<String>,
    val encryptParameters: Array<String>,
    /**
     * 是否格式化日志
     */
    val format: Boolean,
    /**
     * 忽略超时
     */
    val ignoredTimeout: Boolean, val timeoutAlarmSeconds: Int
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is RequestLoggingConfig) return false

        if (includeRequestBody != other.includeRequestBody) return false
        if (includeResponseBody != other.includeResponseBody) return false
        if (includeTrace != other.includeTrace) return false
        if (!encryptHeaders.contentEquals(other.encryptHeaders)) return false
        if (!encryptParameters.contentEquals(other.encryptParameters)) return false
        if (format != other.format) return false
        if (ignoredTimeout != other.ignoredTimeout) return false

        return true
    }

    override fun hashCode(): Int {
        var result = includeRequestBody.hashCode()
        result = 31 * result + includeResponseBody.hashCode()
        result = 31 * result + includeTrace.hashCode()
        result = 31 * result + encryptHeaders.contentHashCode()
        result = 31 * result + encryptParameters.contentHashCode()
        result = 31 * result + format.hashCode()
        result = 31 * result + ignoredTimeout.hashCode()
        return result
    }


}