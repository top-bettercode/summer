package top.bettercode.summer.security

import top.bettercode.summer.security.authorize.ConfigAuthority
import java.lang.annotation.Inherited

@Target(AnnotationTarget.FUNCTION, AnnotationTarget.PROPERTY_GETTER, AnnotationTarget.PROPERTY_SETTER, AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
@Inherited
@MustBeDocumented
@ConfigAuthority("emp")
annotation class EmpAuth  