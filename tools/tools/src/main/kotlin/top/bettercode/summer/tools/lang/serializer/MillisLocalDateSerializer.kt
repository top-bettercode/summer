package top.bettercode.summer.tools.lang.serializer

import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.databind.*
import com.fasterxml.jackson.databind.ser.ContextualSerializer
import com.fasterxml.jackson.databind.ser.std.StdSerializer
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateSerializer
import top.bettercode.summer.tools.lang.util.TimeUtil.Companion.of
import java.io.IOException
import java.time.LocalDate

/**
 * Deserializer for Java 8 temporal [LocalDate]s.
 */
class MillisLocalDateSerializer(
    private val delegate: LocalDateSerializer,
    private val writeDatesAsTimestamps: Boolean
) : StdSerializer<LocalDate>(LocalDate::class.java), ContextualSerializer {

    @Throws(IOException::class)
    override fun serialize(date: LocalDate, gen: JsonGenerator, provider: SerializerProvider?) {
        try {
            if (this.writeDatesAsTimestamps) {
                gen.writeNumber(of(date).toMillis())
                return
            }
        } catch (ignored: Exception) {
        }
        delegate.serialize(date, gen, provider)
    }


    @Throws(JsonMappingException::class)
    override fun createContextual(
        ctxt: SerializerProvider,
        property: BeanProperty?
    ): JsonSerializer<*> {
        val contextual = delegate.createContextual(ctxt, property)
        return MillisLocalDateSerializer(
            contextual as LocalDateSerializer,
            writeDatesAsTimestamps
        )
    }
}
