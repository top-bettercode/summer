package top.bettercode.summer.web.serializer.annotation

import com.fasterxml.jackson.annotation.JacksonAnnotationsInside
import com.fasterxml.jackson.databind.annotation.JsonSerialize
import top.bettercode.summer.web.serializer.PinyinFormat
import top.bettercode.summer.web.serializer.PinyinSerializer

@Target(
    AnnotationTarget.FUNCTION,
    AnnotationTarget.PROPERTY_GETTER,
    AnnotationTarget.PROPERTY_SETTER,
    AnnotationTarget.FIELD
)
@Retention(AnnotationRetention.RUNTIME)
@JacksonAnnotationsInside
@JsonSerialize(using = PinyinSerializer::class)
annotation class JsonPinyin(
    /**
     * @return 分割符
     */
    val separator: String = "",
    /**
     * @return 拼音格式
     */
    val format: PinyinFormat = PinyinFormat.WITHOUT_TONE
)
