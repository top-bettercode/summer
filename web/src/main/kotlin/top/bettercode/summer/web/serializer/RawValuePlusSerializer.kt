package top.bettercode.summer.web.serializer

import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.databind.SerializerProvider
import com.fasterxml.jackson.databind.ser.std.RawSerializer

class RawValuePlusSerializer<T> : RawSerializer<T>(String::class.java) {
    override fun serialize(value: T, gen: JsonGenerator, provider: SerializerProvider) {
        val content = value.toString()
        if (content.isNotBlank()) {
            super.serialize(value, gen, provider)
        } else {
            gen.writeNull()
        }
    }

}
