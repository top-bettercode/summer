package top.bettercode.summer.web.serializer

import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.JsonDeserializer
import org.slf4j.LoggerFactory
import top.bettercode.summer.web.support.EmbeddedIdConverter.toEmbeddedId

/**
 * @author Peter Wu
 */
class EmbeddedIdDeserializer : JsonDeserializer<Any?>() {

    private val log = LoggerFactory.getLogger(EmbeddedIdDeserializer::class.java)

    override fun deserialize(p: JsonParser, ctxt: DeserializationContext): Any? {
        val currentValue = p.currentValue
        val currentName = p.currentName
        val targetType = currentValue.javaClass.getDeclaredField(currentName).type
        val src = p.valueAsString
        return toEmbeddedId(src, targetType)
    }

}
