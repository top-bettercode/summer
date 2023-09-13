package top.bettercode.summer.web.resolver

import com.fasterxml.jackson.annotation.JacksonAnnotationsInside
import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import com.fasterxml.jackson.databind.annotation.JsonSerialize
import top.bettercode.summer.web.serializer.UnitDeserializer
import top.bettercode.summer.web.serializer.UnitSerializer

/**
 * 数量单位转换
 *
 * @author Peter Wu
 */
@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.FIELD, AnnotationTarget.FUNCTION, AnnotationTarget.PROPERTY_GETTER, AnnotationTarget.PROPERTY_SETTER, AnnotationTarget.VALUE_PARAMETER)
@JacksonAnnotationsInside
@JsonSerialize(using = UnitSerializer::class)
@JsonDeserialize(using = UnitDeserializer::class)
annotation class Unit(
        /**
         * 单位转换进制
         */
        val value: Int = 100,
        /**
         * 小数点后位数
         */
        val scale: Int = 2
)
