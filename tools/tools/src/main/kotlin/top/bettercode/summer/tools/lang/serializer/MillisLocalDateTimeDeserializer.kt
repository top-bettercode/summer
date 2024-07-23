package top.bettercode.summer.tools.lang.serializer

import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.BeanProperty
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.JsonDeserializer
import com.fasterxml.jackson.databind.JsonMappingException
import com.fasterxml.jackson.databind.deser.ContextualDeserializer
import com.fasterxml.jackson.databind.deser.std.StdDeserializer
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer
import top.bettercode.summer.tools.lang.util.TimeUtil.Companion.toLocalDateTime
import java.io.IOException
import java.time.LocalDateTime

/**
 * Deserializer for Java 8 temporal [LocalDateTime]s.
 */
class MillisLocalDateTimeDeserializer(
    private val delegate: LocalDateTimeDeserializer,
    private val writeDatesAsTimestamps: Boolean
) : StdDeserializer<LocalDateTime>(LocalDateTime::class.java), ContextualDeserializer {


    @Throws(IOException::class)
    override fun deserialize(parser: JsonParser, context: DeserializationContext): LocalDateTime? {
        try {
            if (writeDatesAsTimestamps) {
                val valueAsString = parser.valueAsString
                return if (valueAsString.isNullOrBlank()) {
                    null
                } else {
                    return toLocalDateTime(parser.longValue)
                }
            }
        } catch (ignored: Exception) {
        }
        return delegate.deserialize(parser, context)
    }

    @Throws(JsonMappingException::class)
    override fun createContextual(
        ctxt: DeserializationContext,
        property: BeanProperty?
    ): JsonDeserializer<*> {
        val contextual = delegate.createContextual(ctxt, property)
        return MillisLocalDateTimeDeserializer(
            contextual as LocalDateTimeDeserializer,
            writeDatesAsTimestamps
        )
    }
}
