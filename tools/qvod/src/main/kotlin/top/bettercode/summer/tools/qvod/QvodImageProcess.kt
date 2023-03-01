package top.bettercode.summer.tools.qvod

import com.fasterxml.jackson.annotation.JacksonAnnotationsInside
import com.fasterxml.jackson.databind.annotation.JsonSerialize
import java.lang.annotation.Inherited

/**
 * 增加 图片即时处理 链接
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
@JsonSerialize(using = QvodImageProcessSerializer::class)
annotation class QvodImageProcess(

    /**
     * @return 图片即时处理模板ID,默认使用系统配置ID
     */
    val value: String = "",

    /**
     * @return 字符串分隔符, 字符串以分隔符分隔后序列化
     */
    val separator: String = "",
)