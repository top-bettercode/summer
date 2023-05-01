package top.bettercode.summer.security.authorize

import java.lang.annotation.Inherited

/**
 * 匿名权限
 *
 * @author Peter Wu
 */
@Target(AnnotationTarget.FUNCTION, AnnotationTarget.PROPERTY_GETTER, AnnotationTarget.PROPERTY_SETTER, AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
@Inherited
@MustBeDocumented
@ConfigAuthority(Anonymous.ROLE_ANONYMOUS_VALUE)
annotation class Anonymous {
    companion object {
        const val ROLE_ANONYMOUS_VALUE = "ROLE_ANONYMOUS"
    }
}
