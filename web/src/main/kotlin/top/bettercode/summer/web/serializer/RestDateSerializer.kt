package top.bettercode.summer.web.serializer

import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.SerializerProvider
import com.fasterxml.jackson.databind.annotation.JacksonStdImpl
import com.fasterxml.jackson.databind.ser.std.StdScalarSerializer
import java.lang.reflect.Type
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.util.*

/**
 * 剩余多少天到期
 *
 * @author Peter Wu
 */
@JacksonStdImpl
class RestDateSerializer : StdScalarSerializer<Date>(Date::class.java, false) {
    override fun isEmpty(provider: SerializerProvider, value: Date): Boolean {
        return false
    }

    override fun getSchema(serializers: SerializerProvider, typeHint: Type): JsonNode {
        return createSchemaNode("number", true)
    }

    override fun serialize(value: Date, gen: JsonGenerator, provider: SerializerProvider) {
        gen.writeNumber(daysOfTwo(Date(), value))
    }

    private fun daysOfTwo(fDate: Date, oDate: Date): Long {
        val localDate = LocalDateTime
                .ofInstant(Instant.ofEpochMilli(fDate.time), ZoneOffset.of("+8"))
        val localDate2 = LocalDateTime
                .ofInstant(Instant.ofEpochMilli(oDate.time), ZoneOffset
                        .of("+8"))
        val days = localDate2.toLocalDate().toEpochDay() - localDate.toLocalDate().toEpochDay()
        return if (days >= 0) days else 0
    }

    companion object {
        private const val serialVersionUID = 1L
    }
}
