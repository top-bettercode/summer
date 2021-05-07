package cn.bestwu.logging.annotation

@Target(AnnotationTarget.CLASS, AnnotationTarget.FUNCTION)
@Retention
@MustBeDocumented
annotation class RequestLogging(
    val includeRequestBody: Boolean = true,
    val includeResponseBody: Boolean = true,
    val includeTrace: Boolean = true,
    val encryptHeaders: Array<String> = [],
    val encryptParameters: Array<String> = [],
    /**
     * 忽略超时
     */
    val ignoredTimeout: Boolean = false,
    val timeoutAlarmSeconds: Int = 0
)