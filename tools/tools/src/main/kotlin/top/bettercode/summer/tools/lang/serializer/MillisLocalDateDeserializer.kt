package top.bettercode.summer.tools.lang.serializer

import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.BeanProperty
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.JsonDeserializer
import com.fasterxml.jackson.databind.JsonMappingException
import com.fasterxml.jackson.databind.deser.ContextualDeserializer
import com.fasterxml.jackson.databind.deser.std.StdDeserializer
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateDeserializer
import top.bettercode.summer.tools.lang.util.TimeUtil.Companion.toLocalDate
import java.io.IOException
import java.time.LocalDate

/**
 * Deserializer for Java 8 temporal [LocalDate]s.
 */
class MillisLocalDateDeserializer(
    private val delegate: LocalDateDeserializer,
    private val writeDatesAsTimestamps: Boolean
) : StdDeserializer<LocalDate>(LocalDate::class.java), ContextualDeserializer {

    @Throws(IOException::class)
    override fun deserialize(parser: JsonParser, context: DeserializationContext): LocalDate? {
        try {
            if (writeDatesAsTimestamps) {
                val valueAsString = parser.valueAsString
                return if (valueAsString.isNullOrBlank()) {
                    null
                } else {
                    toLocalDate(parser.longValue)
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
        return MillisLocalDateDeserializer(
            contextual as LocalDateDeserializer,
            writeDatesAsTimestamps
        )
    }
}
