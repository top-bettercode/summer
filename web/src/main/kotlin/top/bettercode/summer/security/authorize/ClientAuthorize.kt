package top.bettercode.summer.security.authorize

import java.lang.annotation.Inherited

/**
 * client 权限标识
 *
 * @author Peter Wu
 */
@Target(
    AnnotationTarget.FUNCTION,
    AnnotationTarget.PROPERTY_GETTER,
    AnnotationTarget.PROPERTY_SETTER,
    AnnotationTarget.CLASS
)
@Retention(AnnotationRetention.RUNTIME)
@Inherited
@MustBeDocumented
annotation class ClientAuthorize(
    val scope: Array<String> = [],
)
