package top.bettercode.summer.web.serializer

import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.BeanProperty
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.JsonDeserializer
import com.fasterxml.jackson.databind.JsonMappingException
import com.fasterxml.jackson.databind.deser.ContextualDeserializer
import org.slf4j.LoggerFactory
import top.bettercode.summer.web.serializer.annotation.JsonEmbeddedId
import top.bettercode.summer.web.support.EmbeddedIdConverter
import top.bettercode.summer.web.support.EmbeddedIdConverter.toEmbeddedId
import java.io.IOException

/**
 * @author Peter Wu
 */
class EmbeddedIdDeserializer @JvmOverloads constructor(private val delimiter: String = EmbeddedIdConverter.DELIMITER) : JsonDeserializer<Any?>(), ContextualDeserializer {
    private val log = LoggerFactory.getLogger(EmbeddedIdDeserializer::class.java)
    @Throws(IOException::class)
    override fun deserialize(p: JsonParser, ctxt: DeserializationContext): Any? {
        try {
            val currentValue = p.currentValue
            val currentName = p.currentName
            val targetType = currentValue.javaClass.getDeclaredField(currentName).type
            val src = p.valueAsString
            return toEmbeddedId(src, delimiter, targetType)
        } catch (e: NoSuchFieldException) {
            log.warn("反序列化失败", e)
        }
        return null
    }

    @Throws(JsonMappingException::class)
    override fun createContextual(ctxt: DeserializationContext, property: BeanProperty?): JsonDeserializer<*> {
        var delimiter: String
        if (property != null) {
            val annotation = property.getAnnotation(JsonEmbeddedId::class.java)
            delimiter = annotation.value
            delimiter = if ("" == delimiter) EmbeddedIdConverter.DELIMITER else delimiter
        } else {
            delimiter = EmbeddedIdConverter.DELIMITER
        }
        return EmbeddedIdDeserializer(delimiter)
    }
}
