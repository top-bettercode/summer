package top.bettercode.summer.web.serializer.annotation

import com.fasterxml.jackson.annotation.JacksonAnnotationsInside
import com.fasterxml.jackson.databind.annotation.JsonSerialize
import top.bettercode.summer.web.serializer.CodeSerializer
import top.bettercode.summer.web.support.code.CodeServiceHolder
import java.lang.annotation.Inherited

@Target(AnnotationTarget.FUNCTION, AnnotationTarget.PROPERTY_GETTER, AnnotationTarget.PROPERTY_SETTER, AnnotationTarget.FIELD, AnnotationTarget.ANNOTATION_CLASS)
@Retention(AnnotationRetention.RUNTIME)
@JacksonAnnotationsInside
@Inherited
@JsonSerialize(using = CodeSerializer::class)
annotation class JsonCode(
        /**
         * @return codeType
         */
        val value: String = "",
        /**
         * @return 是否使用扩展的字段序列化
         */
        val extended: Boolean = true,
        /**
         * @return codeService beanName default: [CodeServiceHolder.default]
         */
        val codeServiceRef: String = "") 