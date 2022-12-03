package top.bettercode.summer.tools.qvod

import com.fasterxml.jackson.annotation.JacksonAnnotationsInside
import com.fasterxml.jackson.databind.annotation.JsonSerialize
import top.bettercode.summer.web.serializer.JsonUrlMapper
import java.lang.annotation.Inherited
import kotlin.reflect.KClass

/**
 * 防盗链URL
 *
 * 注解在url字段上
 */
@Target(
    AnnotationTarget.FUNCTION,
    AnnotationTarget.PROPERTY_GETTER,
    AnnotationTarget.PROPERTY_SETTER,
    AnnotationTarget.FIELD,
    AnnotationTarget.ANNOTATION_CLASS
)
@Retention(AnnotationRetention.RUNTIME)
@JacksonAnnotationsInside
@Inherited
@JsonSerialize(using = QvodAntiLeechUrlSerializer::class)
annotation class QvodAntiLeechUrl(
    /**
     * @return 字符串分隔符, 字符串以分隔符分隔后序列化
     */
    val separator: String = "",
    /**
     * @return 对象转换为字符串
     */
    val mapper: KClass<out JsonUrlMapper> = JsonUrlMapper::class

)