package top.bettercode.summer.tools.lang.serializer

import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.BeanProperty
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.JsonDeserializer
import com.fasterxml.jackson.databind.JsonMappingException
import com.fasterxml.jackson.databind.deser.ContextualDeserializer
import com.fasterxml.jackson.databind.deser.std.StdDeserializer
import java.io.IOException
import java.time.LocalDate

/**
 * Deserializer for Java 8 temporal [LocalDate]s.
 */
class PlusDaysMillisLocalDateDeserializer(
    private val delegate: MillisLocalDateDeserializer,
    private val daysToAdd: Long = 0L
) : StdDeserializer<LocalDate>(LocalDate::class.java), ContextualDeserializer {

    @Throws(IOException::class)
    override fun deserialize(parser: JsonParser, context: DeserializationContext): LocalDate? {
        val localDate = delegate.deserialize(parser, context)
        return if (daysToAdd != 0L) {
            localDate?.plusDays(daysToAdd)
        } else {
            localDate
        }
    }

    @Throws(JsonMappingException::class)
    override fun createContextual(
        ctxt: DeserializationContext,
        property: BeanProperty?
    ): JsonDeserializer<*> {
        val contextual = delegate.createContextual(ctxt, property)
        if (property == null) {
            return contextual
        }
        val annotation = property.getAnnotation(PlusDays::class.java)
        return if (annotation == null) {
            contextual
        } else {
            PlusDaysMillisLocalDateDeserializer(
                contextual as MillisLocalDateDeserializer,
                annotation.value
            )
        }
    }
}
