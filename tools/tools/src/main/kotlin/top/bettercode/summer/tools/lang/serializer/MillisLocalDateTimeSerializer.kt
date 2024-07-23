package top.bettercode.summer.tools.lang.serializer

import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.databind.*
import com.fasterxml.jackson.databind.ser.ContextualSerializer
import com.fasterxml.jackson.databind.ser.std.StdSerializer
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer
import top.bettercode.summer.tools.lang.util.TimeUtil.Companion.of
import java.io.IOException
import java.time.LocalDateTime

/**
 * Deserializer for Java 8 temporal [LocalDateTime]s.
 */
class MillisLocalDateTimeSerializer(
    private val delegate: LocalDateTimeSerializer,
    private val writeDatesAsTimestamps: Boolean
) : StdSerializer<LocalDateTime>(LocalDateTime::class.java), ContextualSerializer {

    @Throws(IOException::class)
    override fun serialize(date: LocalDateTime, gen: JsonGenerator, provider: SerializerProvider?) {
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
        return MillisLocalDateTimeSerializer(
            contextual as LocalDateTimeSerializer,
            writeDatesAsTimestamps
        )
    }
}
