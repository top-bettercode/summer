package top.bettercode.summer.tools.lang.serializer

import com.fasterxml.jackson.annotation.JacksonAnnotation
import java.lang.annotation.Inherited

/**
 *
 * @author Peter Wu
 */
@Target(
    AnnotationTarget.FUNCTION,
    AnnotationTarget.PROPERTY_GETTER,
    AnnotationTarget.PROPERTY_SETTER,
    AnnotationTarget.FIELD,
    AnnotationTarget.ANNOTATION_CLASS
)
@Retention(AnnotationRetention.RUNTIME)
@JacksonAnnotation
@Inherited
annotation class PlusDays(val value: Long = 1)
