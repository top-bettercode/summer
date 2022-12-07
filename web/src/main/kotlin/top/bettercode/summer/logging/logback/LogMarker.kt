package top.bettercode.summer.logging.logback

/**
 *
 * @author Peter Wu
 */
@Target(AnnotationTarget.CLASS, AnnotationTarget.FUNCTION)
@Retention
@MustBeDocumented
annotation class LogMarker(
    val value: String
)
