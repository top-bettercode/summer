package top.bettercode.summer.data.jpa.support

import java.lang.annotation.Inherited

/**
 * @author Peter Wu
 */
@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.FUNCTION, AnnotationTarget.PROPERTY_GETTER, AnnotationTarget.PROPERTY_SETTER, AnnotationTarget.ANNOTATION_CLASS)
@MustBeDocumented
@Inherited
annotation class QuerySize(
        /**
         * @return 查询数量
         */
        val value: Int
) 