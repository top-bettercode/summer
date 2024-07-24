package top.bettercode.summer.tools.lang.serializer

import com.fasterxml.jackson.annotation.JacksonAnnotationsInside
import java.lang.annotation.Inherited
import java.time.LocalDate

/**
 *
 * PlusDays [LocalDate].
 *
 * @author Peter Wu
 */
@Target(
    AnnotationTarget.FUNCTION,
    AnnotationTarget.PROPERTY_GETTER,
    AnnotationTarget.PROPERTY_SETTER,
    AnnotationTarget.FIELD,
    AnnotationTarget.VALUE_PARAMETER,
    AnnotationTarget.ANNOTATION_CLASS
)
@Retention(AnnotationRetention.RUNTIME)
@JacksonAnnotationsInside
@Inherited
@PlusDays
annotation class EndOfDay
