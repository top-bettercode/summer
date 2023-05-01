package top.bettercode.summer.web.serializer.annotation

import com.fasterxml.jackson.annotation.JacksonAnnotationsInside
import com.fasterxml.jackson.databind.annotation.JsonSerialize
import top.bettercode.summer.web.serializer.DivisionSerializer

@Target(AnnotationTarget.FUNCTION, AnnotationTarget.PROPERTY_GETTER, AnnotationTarget.PROPERTY_SETTER, AnnotationTarget.FIELD)
@Retention(AnnotationRetention.RUNTIME)
@JacksonAnnotationsInside
@JsonSerialize(using = DivisionSerializer::class)
annotation class JsonDivision(
        /**
         * @return 是否使用虚拟节点
         */
        val value: Boolean = false)
