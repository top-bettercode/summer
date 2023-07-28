package top.bettercode.summer.tools.qvod

import com.fasterxml.jackson.annotation.JacksonAnnotationsInside
import com.fasterxml.jackson.databind.annotation.JsonSerialize
import java.lang.annotation.Inherited

/**
 * 开启防盗链后序列化自动补充相应字段
 *
 * 注解在fileId字段上
 *
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
@JsonSerialize(using = QvodPlaySignatureSerializer::class)
annotation class QvodPlaySignature