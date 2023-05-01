package top.bettercode.summer.web.resolver

import com.fasterxml.jackson.annotation.JacksonAnnotationsInside
import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import com.fasterxml.jackson.databind.annotation.JsonSerialize
import top.bettercode.summer.web.serializer.CentDeserializer
import top.bettercode.summer.web.serializer.CentSerializer

/**
 * 长整形 分 序列化为字符元格式 及反序列化
 *
 * @author Peter Wu
 */
@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.FIELD, AnnotationTarget.FUNCTION, AnnotationTarget.PROPERTY_GETTER, AnnotationTarget.PROPERTY_SETTER, AnnotationTarget.VALUE_PARAMETER)
@JacksonAnnotationsInside
@JsonSerialize(using = CentSerializer::class)
@JsonDeserialize(using = CentDeserializer::class)
annotation class Cent
