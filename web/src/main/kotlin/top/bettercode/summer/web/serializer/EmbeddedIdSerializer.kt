package top.bettercode.summer.web.serializer

import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.databind.SerializerProvider
import com.fasterxml.jackson.databind.annotation.JacksonStdImpl
import com.fasterxml.jackson.databind.ser.std.StdScalarSerializer
import top.bettercode.summer.web.support.EmbeddedIdConverter

@JacksonStdImpl
class EmbeddedIdSerializer : StdScalarSerializer<Any>(Any::class.java) {

    override fun serialize(value: Any, gen: JsonGenerator, provider: SerializerProvider?) {
        gen.writeString(EmbeddedIdConverter.toString(value))
    }
}