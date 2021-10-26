package top.bettercode.logging.annotation

import top.bettercode.logging.RequestLoggingFilter
import java.lang.annotation.Inherited

@Target(AnnotationTarget.CLASS, AnnotationTarget.FUNCTION, AnnotationTarget.ANNOTATION_CLASS)
@Retention
@Inherited
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
    val timeoutAlarmSeconds: Int = 0,
    val logMarker: String = RequestLoggingFilter.REQUEST_LOG_MARKER
)