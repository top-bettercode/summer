package top.bettercode.summer.security.authorize

import java.lang.annotation.Inherited

/**
 * 接口 权限标识
 *
 * @author Peter Wu
 */
@Target(AnnotationTarget.FUNCTION, AnnotationTarget.PROPERTY_GETTER, AnnotationTarget.PROPERTY_SETTER, AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
@Inherited
@MustBeDocumented
@Repeatable
annotation class ConfigAuthority(
        /**
         * @return 需要的权限标识，有任意其中一个即可，不能为null或空白字符串
         */
        vararg val value: String) {
    @MustBeDocumented
    @Retention(AnnotationRetention.RUNTIME)
    @Inherited
    @Target(AnnotationTarget.FUNCTION, AnnotationTarget.PROPERTY_GETTER, AnnotationTarget.PROPERTY_SETTER, AnnotationTarget.CLASS)
    annotation class List(vararg val value: ConfigAuthority)
}
