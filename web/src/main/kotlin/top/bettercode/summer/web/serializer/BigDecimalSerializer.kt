package top.bettercode.summer.web.serializer

import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.databind.BeanProperty
import com.fasterxml.jackson.databind.JsonSerializer
import com.fasterxml.jackson.databind.SerializerProvider
import com.fasterxml.jackson.databind.annotation.JacksonStdImpl
import com.fasterxml.jackson.databind.jsontype.TypeSerializer
import com.fasterxml.jackson.databind.ser.ContextualSerializer
import com.fasterxml.jackson.databind.ser.std.StdScalarSerializer
import top.bettercode.summer.web.serializer.annotation.JsonBigDecimal
import java.math.BigDecimal
import java.math.RoundingMode

@JacksonStdImpl
class BigDecimalSerializer @JvmOverloads constructor(
    private val scale: Int = 2,
    private val divisor: BigDecimal? = null,
    private val roundingMode: RoundingMode = RoundingMode.HALF_UP,
    private val toPlainString: Boolean = false,
    private val stripTrailingZeros: Boolean = false,
    private val percent: Boolean = false
) : StdScalarSerializer<Number>(Number::class.java), ContextualSerializer {
    override fun serialize(value: Number, gen: JsonGenerator, provider: SerializerProvider?) {
        var scale = scale
        var content = when (value) {
            is BigDecimal -> value
            else -> BigDecimal(value.toString())
        }
        if (divisor != null) {
            content = content.divide(divisor, roundingMode)
        }
        if (scale == -1) {
            scale = content.scale()
        } else if (content.scale() != scale) {
            content = content.setScale(scale, roundingMode)
        }
        if (stripTrailingZeros) {
            content = content.stripTrailingZeros()
        }
        if (toPlainString) {
            gen.writeString(content.toPlainString())
        } else {
            gen.writeNumber(content.toPlainString().toBigDecimal())
        }
        if (percent) {
            val outputContext = gen.outputContext
            val fieldName = outputContext.currentName
            var percent = content.multiply(BigDecimal(100))
                .setScale(scale - 2, roundingMode)
            if (stripTrailingZeros) {
                percent = percent.stripTrailingZeros()
            }
            gen.writeStringField(fieldName + "Pct", "${percent.toPlainString()}%")
        }
    }

    override fun serializeWithType(
        value: Number,
        gen: JsonGenerator,
        provider: SerializerProvider,
        typeSer: TypeSerializer
    ) {
        serialize(value, gen, provider)
    }


    override fun createContextual(
        prov: SerializerProvider,
        property: BeanProperty?
    ): JsonSerializer<*> {
        if (property != null) {
            val annotation = property.getAnnotation(JsonBigDecimal::class.java)
                ?: throw RuntimeException("未注解@" + JsonBigDecimal::class.java.name)
            val divisor = annotation.divisor
            return BigDecimalSerializer(
                annotation.scale,
                if ("" == divisor) null else BigDecimal(divisor),
                annotation.roundingMode,
                annotation.toPlainString,
                annotation.stripTrailingZeros,
                annotation.percent
            )
        }
        return this
    }

}