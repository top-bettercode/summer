package top.bettercode.summer.logging.annotation

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
