package top.bettercode.summer.web.serializer

import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.databind.SerializerProvider
import com.fasterxml.jackson.databind.annotation.JacksonStdImpl
import com.fasterxml.jackson.databind.ser.std.NumberSerializer
import top.bettercode.summer.tools.lang.util.MoneyUtil.toYun
import java.io.IOException
import java.math.BigDecimal

@JacksonStdImpl
class CentSerializer : NumberSerializer(Long::class.java) {
    @Throws(IOException::class)
    override fun serialize(value: Number, gen: JsonGenerator,
                           provider: SerializerProvider) {
        gen.writeString(toYun(BigDecimal(value.toString()), newScale).toString())
    }

    companion object {
        private const val serialVersionUID = 1L
        private var newScale = 2
        fun setNewScale(newScale: Int) {
            Companion.newScale = newScale
        }
    }
}