package top.bettercode.summer.web.serializer.annotation

import com.fasterxml.jackson.annotation.JacksonAnnotationsInside
import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import com.fasterxml.jackson.databind.annotation.JsonSerialize
import top.bettercode.summer.web.serializer.StringToSetDeserializer
import top.bettercode.summer.web.serializer.SetToStringSerializer
import java.lang.annotation.Inherited

/**
 * @author Peter Wu
 */
@Target(AnnotationTarget.FUNCTION, AnnotationTarget.PROPERTY_GETTER, AnnotationTarget.PROPERTY_SETTER, AnnotationTarget.FIELD, AnnotationTarget.ANNOTATION_CLASS)
@Retention(AnnotationRetention.RUNTIME)
@JacksonAnnotationsInside
@Inherited
@JsonSerialize(using = SetToStringSerializer::class)
@JsonDeserialize(using = StringToSetDeserializer::class)
annotation class JsonSetToString(
        /**
         * @return 字符串分隔符
         */
        val value: String = ",",
        /**
         * @return 是否使用扩展的字段
         */
        val extended: Boolean = true)
