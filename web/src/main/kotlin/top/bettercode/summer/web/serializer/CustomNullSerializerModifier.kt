package top.bettercode.summer.web.serializer

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.databind.BeanDescription
import com.fasterxml.jackson.databind.SerializationConfig
import com.fasterxml.jackson.databind.ser.BeanPropertyWriter
import com.fasterxml.jackson.databind.ser.BeanSerializerModifier
import top.bettercode.summer.web.properties.JacksonExtProperties
import top.bettercode.summer.web.serializer.annotation.JsonDefault

class CustomNullSerializerModifier(
        private val jacksonExtProperties: JacksonExtProperties) : BeanSerializerModifier() {
    override fun changeProperties(config: SerializationConfig,
                                  beanDesc: BeanDescription,
                                  beanProperties: List<BeanPropertyWriter>): List<BeanPropertyWriter> {
        for (writer in beanProperties) {
            if (!writer.hasNullSerializer()) {
                val annotation = writer.getAnnotation(JsonDefault::class.java)
                var defaultValue: String? = null
                var extendedValue: String? = null
                var fieldName: String? = null
                if (annotation != null) {
                    defaultValue = annotation.value
                    extendedValue = annotation.extended
                    fieldName = annotation.fieldName
                }
                if (defaultValue != null || fieldName != null || config.defaultPropertyInclusion.valueInclusion != JsonInclude.Include.NON_NULL) {
                    writer.assignNullSerializer(CustomNullSerializer(writer, defaultValue, fieldName,
                            extendedValue,
                            jacksonExtProperties))
                }
            }
        }
        return beanProperties
    }
}