package top.bettercode.summer.web.serializer

import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.databind.BeanProperty
import com.fasterxml.jackson.databind.JsonSerializer
import com.fasterxml.jackson.databind.SerializerProvider
import com.fasterxml.jackson.databind.annotation.JacksonStdImpl
import com.fasterxml.jackson.databind.ser.ContextualSerializer
import com.fasterxml.jackson.databind.ser.std.NumberSerializer
import top.bettercode.summer.web.resolver.UnitConveter
import top.bettercode.summer.web.resolver.UnitGenericConverter

@JacksonStdImpl
class UnitSerializer @JvmOverloads
constructor(private val unitValue: Int = 100,
            private val scale: Int = 2)
    : NumberSerializer(Number::class.java), ContextualSerializer {

    override fun serialize(value: Number, gen: JsonGenerator,
                           provider: SerializerProvider) {
        val string = UnitGenericConverter.larger(number = value, value = unitValue, scale = scale).toPlainString()

        gen.writeString(string)
    }

    override fun createContextual(prov: SerializerProvider?, property: BeanProperty?): JsonSerializer<*> {
        if (property != null) {
            val annotation = property.getAnnotation(UnitConveter::class.java)
                    ?: throw RuntimeException("未注解@" + UnitConveter::class.java.name)

            return UnitSerializer(annotation.value, annotation.scale)
        }
        return super.createContextual(prov, null)
    }
}