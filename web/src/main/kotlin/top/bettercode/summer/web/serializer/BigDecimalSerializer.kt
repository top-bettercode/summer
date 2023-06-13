package top.bettercode.summer.web.serializer

import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.databind.BeanProperty
import com.fasterxml.jackson.databind.JsonSerializer
import com.fasterxml.jackson.databind.SerializerProvider
import com.fasterxml.jackson.databind.annotation.JacksonStdImpl
import com.fasterxml.jackson.databind.ser.ContextualSerializer
import com.fasterxml.jackson.databind.ser.std.StdScalarSerializer
import org.springframework.util.StringUtils
import top.bettercode.summer.web.serializer.annotation.JsonBigDecimal
import java.io.IOException
import java.math.BigDecimal
import java.math.RoundingMode

@JacksonStdImpl
class BigDecimalSerializer @JvmOverloads constructor(private val scale: Int = 2, private val divisor: BigDecimal? = null, private val roundingMode: RoundingMode = RoundingMode.HALF_UP, private val toPlainString: Boolean = false,
                                                     private val reduceFraction: Boolean = false,
                                                     private val percent: Boolean = false) : StdScalarSerializer<BigDecimal>(BigDecimal::class.java), ContextualSerializer {
    override fun serialize(value: BigDecimal, gen: JsonGenerator, provider: SerializerProvider?) {
        var scale = scale
        var content = value
        if (divisor != null) {
            content = content.divide(divisor, roundingMode)
        }
        if (scale == -1) {
            scale = content.scale()
        } else if (content.scale() != scale) {
            content = content.setScale(scale, roundingMode)
        }
        var plainString = content.toPlainString()
        if (reduceFraction) {
            plainString = reduceFraction(plainString)
            content = BigDecimal(plainString)
        }
        if (toPlainString) {
            gen.writeString(plainString)
        } else {
            gen.writeNumber(content)
        }
        if (percent) {
            val outputContext = gen.outputContext
            val fieldName = outputContext.currentName
            var percentPlainSring = content.multiply(BigDecimal(100))
                    .setScale(scale - 2, roundingMode).toPlainString()
            if (reduceFraction) {
                percentPlainSring = reduceFraction(percentPlainSring)
            }
            gen.writeStringField(fieldName + "Pct", "$percentPlainSring%")
        }
    }

    private fun reduceFraction(plainString: String): String {
        return if (plainString.contains(".")) StringUtils
                .trimTrailingCharacter(StringUtils.trimTrailingCharacter(plainString, '0'), '.') else plainString
    }

    override fun createContextual(prov: SerializerProvider, property: BeanProperty?): JsonSerializer<*> {
        if (property != null) {
            val annotation = property.getAnnotation(JsonBigDecimal::class.java)
                    ?: throw RuntimeException("未注解@" + JsonBigDecimal::class.java.name)
            val divisor = annotation.divisor
            return BigDecimalSerializer(annotation.scale, if ("" == divisor) null else BigDecimal(divisor), annotation.roundingMode,
                    annotation.toPlainString,
                    annotation.reduceFraction, annotation.percent)
        }
        return this
    }

    companion object {
        private const val serialVersionUID = 1L
    }
}