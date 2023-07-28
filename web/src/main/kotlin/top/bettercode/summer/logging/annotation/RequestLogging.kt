package top.bettercode.summer.logging.annotation

import top.bettercode.summer.tools.lang.operation.HttpOperation
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
        val logMarker: String = HttpOperation.REQUEST_LOG_MARKER
)