package top.bettercode.summer.web.serializer.annotation

import com.fasterxml.jackson.annotation.JacksonAnnotationsInside
import com.fasterxml.jackson.databind.annotation.JsonSerialize
import top.bettercode.summer.web.serializer.JsonUrlMapper
import top.bettercode.summer.web.serializer.UrlSerializer
import java.lang.annotation.Inherited
import kotlin.reflect.KClass

/**
 * @author Peter Wu
 */
@Target(AnnotationTarget.FUNCTION, AnnotationTarget.PROPERTY_GETTER, AnnotationTarget.PROPERTY_SETTER, AnnotationTarget.FIELD, AnnotationTarget.ANNOTATION_CLASS)
@Retention(AnnotationRetention.RUNTIME)
@JacksonAnnotationsInside
@Inherited
@JsonSerialize(using = UrlSerializer::class)
annotation class JsonUrl(
        /**
         * @return URL路径前缀配置 expression: e.g. "${summer.multipart.file-url-format}".
         */
        val value: String = "",
        /**
         * @return 默认值
         */
        val defaultValue: String = "",
        /**
         * @return url字段名称，默认为空表示在原字段后加Url后缀
         */
        val urlFieldName: String = "",
        /**
         * @return 是否使用扩展的字段序列化URL
         */
        val extended: Boolean = true,
        /**
         * @return collection serialize as map
         */
        val asMap: Boolean = false,
        /**
         * @return 字符串分隔符, 当分隔符不为空时，字符串以分隔符分隔后序列化为数组
         */
        val separator: String = "",
        /**
         * @return 对象转换为字符串
         */
        val mapper: KClass<out JsonUrlMapper> = JsonUrlMapper::class)
