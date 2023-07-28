package top.bettercode.summer.web.serializer

import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.databind.SerializerProvider
import com.fasterxml.jackson.databind.annotation.JacksonStdImpl
import com.fasterxml.jackson.databind.ser.std.NumberSerializer
import top.bettercode.summer.web.support.KilogramUtil.toKilogram
import java.math.BigDecimal

@JacksonStdImpl
class KilogramSerializer : NumberSerializer(Long::class.java) {
    override fun serialize(value: Number, gen: JsonGenerator,
                           provider: SerializerProvider) {
        gen.writeString(toKilogram(BigDecimal(value.toString()), newScale).toString())
    }

    companion object {
        private const val serialVersionUID = 1L
        private var newScale = 3
        fun setNewScale(newScale: Int) {
            Companion.newScale = newScale
        }
    }
}