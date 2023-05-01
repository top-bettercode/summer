package top.bettercode.summer.web.serializer.annotation

import java.lang.annotation.Inherited

@Target(AnnotationTarget.FUNCTION, AnnotationTarget.PROPERTY_GETTER, AnnotationTarget.PROPERTY_SETTER, AnnotationTarget.FIELD, AnnotationTarget.ANNOTATION_CLASS)
@Retention(AnnotationRetention.RUNTIME)
@Inherited
annotation class JsonDefault(
        /**
         * @return 默认值
         */
        val value: String = "",
        /**
         * @return 使用另一个字段的值
         */
        val fieldName: String = "",
        /**
         * @return 扩展默认值
         */
        val extended: String = "") 