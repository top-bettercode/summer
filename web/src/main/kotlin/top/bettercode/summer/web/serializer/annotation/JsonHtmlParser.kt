package top.bettercode.summer.web.serializer.annotation

import com.fasterxml.jackson.annotation.JacksonAnnotationsInside
import com.fasterxml.jackson.databind.annotation.JsonSerialize
import top.bettercode.summer.web.serializer.HtmlParserSerializer
import java.lang.annotation.Inherited

@Target(AnnotationTarget.FUNCTION, AnnotationTarget.PROPERTY_GETTER, AnnotationTarget.PROPERTY_SETTER, AnnotationTarget.FIELD, AnnotationTarget.ANNOTATION_CLASS)
@Retention(AnnotationRetention.RUNTIME)
@JacksonAnnotationsInside
@Inherited
@JsonSerialize(using = HtmlParserSerializer::class)
annotation class JsonHtmlParser(
        /**
         * @return length
         */
        val value: Int = -1) 