package top.bettercode.summer.web.serializer.annotation

import com.fasterxml.jackson.annotation.JacksonAnnotationsInside
import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import com.fasterxml.jackson.databind.annotation.JsonSerialize
import top.bettercode.summer.web.serializer.EmbeddedIdDeserializer
import top.bettercode.summer.web.serializer.EmbeddedIdSerializer

/**
 * @author Peter Wu
 * @since 0.1.15
 */
@Target(
    AnnotationTarget.FUNCTION,
    AnnotationTarget.PROPERTY_GETTER,
    AnnotationTarget.PROPERTY_SETTER,
    AnnotationTarget.FIELD
)
@Retention(AnnotationRetention.RUNTIME)
@JacksonAnnotationsInside
@JsonSerialize(using = EmbeddedIdSerializer::class)
@JsonDeserialize(using = EmbeddedIdDeserializer::class)
annotation class JsonEmbeddedId
