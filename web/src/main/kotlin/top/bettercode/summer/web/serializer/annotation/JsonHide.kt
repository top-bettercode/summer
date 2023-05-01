package top.bettercode.summer.web.serializer.annotation

import com.fasterxml.jackson.annotation.JacksonAnnotationsInside
import com.fasterxml.jackson.databind.annotation.JsonSerialize
import top.bettercode.summer.web.serializer.HideSerializer
import java.lang.annotation.Inherited

/**
 * @author Peter Wu
 * @since 0.1.15
 */
@Target(AnnotationTarget.FUNCTION, AnnotationTarget.PROPERTY_GETTER, AnnotationTarget.PROPERTY_SETTER, AnnotationTarget.FIELD, AnnotationTarget.ANNOTATION_CLASS)
@Retention(AnnotationRetention.RUNTIME)
@JacksonAnnotationsInside
@Inherited
@JsonSerialize(using = HideSerializer::class)
annotation class JsonHide(val beginKeep: Int = 0, val endKeep: Int = 0, val alwaysHide: Boolean = true)
