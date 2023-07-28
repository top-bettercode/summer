package top.bettercode.summer.web.serializer

import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.databind.BeanProperty
import com.fasterxml.jackson.databind.JsonSerializer
import com.fasterxml.jackson.databind.SerializerProvider
import com.fasterxml.jackson.databind.annotation.JacksonStdImpl
import com.fasterxml.jackson.databind.ser.ContextualSerializer
import com.fasterxml.jackson.databind.ser.std.StdScalarSerializer
import top.bettercode.summer.web.serializer.annotation.JsonEmbeddedId
import top.bettercode.summer.web.support.EmbeddedIdConverter
import top.bettercode.summer.web.support.EmbeddedIdConverter.toString

@JacksonStdImpl
class EmbeddedIdSerializer @JvmOverloads constructor(private val delimiter: String = EmbeddedIdConverter.DELIMITER) : StdScalarSerializer<Any>(Any::class.java), ContextualSerializer {
    override fun serialize(value: Any, gen: JsonGenerator, provider: SerializerProvider?) {
        gen.writeString(toString(value, delimiter))
    }

    override fun createContextual(prov: SerializerProvider, property: BeanProperty?): JsonSerializer<*> {
        if (property != null) {
            val annotation = property.getAnnotation(JsonEmbeddedId::class.java)
            var delimiter = annotation.value
            delimiter = if ("" == delimiter) EmbeddedIdConverter.DELIMITER else delimiter
            return EmbeddedIdSerializer(delimiter)
        }
        return prov.findNullValueSerializer(null)
    }

    companion object {
        private const val serialVersionUID = 1L
    }
}