package top.bettercode.summer.web.serializer

import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.databind.SerializerProvider
import com.fasterxml.jackson.databind.jsontype.TypeSerializer
import com.fasterxml.jackson.databind.ser.BeanPropertyWriter
import com.fasterxml.jackson.databind.ser.std.StdSerializer
import org.springframework.util.StringUtils
import top.bettercode.summer.tools.lang.util.DirectFieldAccessFallbackBeanWrapper
import top.bettercode.summer.web.properties.JacksonExtProperties
import top.bettercode.summer.web.serializer.annotation.JsonBigDecimal
import top.bettercode.summer.web.serializer.annotation.JsonCode
import top.bettercode.summer.web.serializer.annotation.JsonUrl
import top.bettercode.summer.web.support.ApplicationContextHolder
import java.io.Serializable
import java.math.BigDecimal
import java.util.*

/**
 * @author Peter Wu
 */
class CustomNullSerializer(private val writer: BeanPropertyWriter, private val defaultValue: String?,
                           private val fieldName: String?, private val extendedValue: String?, private val jacksonExtProperties: JacksonExtProperties) : StdSerializer<Any>(Any::class.java) {
    private val type: Class<*> = writer.type.rawClass
    private val isArray: Boolean = type.isArray || MutableCollection::class.java.isAssignableFrom(type) && !MutableMap::class.java
            .isAssignableFrom(type)

    override fun serialize(value: Any?, gen: JsonGenerator, provider: SerializerProvider) {
        val outputContext = gen.outputContext
        val fieldName = outputContext.currentName
        var defaultValue: Any? = defaultValue
        if ("" == this.defaultValue && StringUtils.hasText(this.fieldName)) {
            val o = gen.currentValue
            val beanWrapper = DirectFieldAccessFallbackBeanWrapper(
                    o)
            defaultValue = beanWrapper.getPropertyValue(this.fieldName!!)
        }
        if (defaultValue == null) {
            if (jacksonExtProperties.isDefaultEmpty) {
                serializeJsonCode(value, gen, provider, fieldName)
            } else {
                gen.writeNull()
                serializeExtend(gen, fieldName, false)
            }
        } else {
            if (defaultValue is String && !StringUtils.hasText(defaultValue as String?)) {
                if (serializeJsonCode(value, gen, provider, fieldName)) {
                    return
                }
                return
            }
            val jsonBigDecimal = writer.getAnnotation(JsonBigDecimal::class.java)
            val `val` = ApplicationContextHolder.conversionService.convert(defaultValue, type)
            if (jsonBigDecimal != null) {
                val divisor = jsonBigDecimal.divisor
                BigDecimalSerializer(jsonBigDecimal.scale, if ("" == divisor) null else BigDecimal(divisor), jsonBigDecimal.roundingMode,
                        jsonBigDecimal.toPlainString,
                        jsonBigDecimal.reduceFraction, jsonBigDecimal.percent).serialize(
                        `val` as BigDecimal, gen,
                        provider)
                return
            }
            val jsonCode = writer.getAnnotation(JsonCode::class.java)
            if (jsonCode != null) {
                CodeSerializer(jsonCode.value, jsonCode.extended)
                        .serialize(`val` as Serializable, gen, provider)
                return
            }
            val jsonUrl = writer.getAnnotation(JsonUrl::class.java)
            if (jsonUrl != null && `val` != null) {
                UrlSerializer(jsonUrl.value, jsonUrl.urlFieldName, jsonUrl.extended,
                        jsonUrl.asMap,
                        jsonUrl.separator, jsonUrl.defaultValue, jsonUrl.mapper).serialize(`val`, gen,
                        provider)
                return
            }
            gen.writeObject(`val`)
        }
    }

    private fun serializeJsonCode(value: Any?, gen: JsonGenerator, provider: SerializerProvider,
                                  fieldName: String): Boolean {
        val jsonCode = writer.getAnnotation(JsonCode::class.java)
        if (jsonCode != null) {
            CodeSerializer(jsonCode.value, jsonCode.extended)
                    .serialize("", gen, provider)
            return true
        }
        serializeNull(gen, type, value)
        serializeExtend(gen, fieldName, true)
        return false
    }

    private fun serializeExtend(gen: JsonGenerator, fieldName: String,
                                defaultEmpty: Boolean) {
        val value = if (StringUtils.hasText(extendedValue)) extendedValue else if (defaultEmpty) "" else null
        val jsonCode = writer.getAnnotation(JsonCode::class.java)
        if (jsonCode != null && jsonCode.extended) {
            gen.writeStringField(fieldName + "Name", value)
            return
        }
        val jsonBigDecimal = writer.getAnnotation(JsonBigDecimal::class.java)
        if (jsonBigDecimal != null && jsonBigDecimal.percent) {
            gen.writeStringField(fieldName + "Pct", value)
            return
        }
        val jsonUrl = writer.getAnnotation(JsonUrl::class.java)
        if (jsonUrl != null && jsonUrl.extended) {
            var urlFieldName = jsonUrl.urlFieldName
            if ("" == urlFieldName) {
                urlFieldName = if (isArray) {
                    fieldName + "Urls"
                } else {
                    fieldName + "Url"
                }
            }
            if (isArray) {
                gen.writeObjectField(urlFieldName, if (defaultEmpty) Collections.EMPTY_LIST else null)
            } else {
                gen.writeStringField(urlFieldName, value)
            }
        }
    }

    fun serializeNull(gen: JsonGenerator, type: Class<*>, value: Any?) {
        if (type == String::class.java) {
            gen.writeString("")
        } else if (isArray) {
            gen.writeObject(Collections.EMPTY_LIST)
        } else if (type.classLoader != null || MutableMap::class.java.isAssignableFrom(type)) {
            gen.writeObject(emptyMap<Any, Any>())
        } else {
            gen.writeObject(value)
        }
    }

    override fun serializeWithType(value: Any, gen: JsonGenerator, provider: SerializerProvider,
                                   typeSer: TypeSerializer) {
        serialize(value, gen, provider)
    }

    companion object {
        fun support(type: Class<*>): Boolean {
            return type == String::class.java || type.isArray || (MutableCollection::class.java.isAssignableFrom(type)
                    && !MutableMap::class.java
                    .isAssignableFrom(type)) || type.classLoader != null || MutableMap::class.java
                    .isAssignableFrom(type)
        }
    }
}
