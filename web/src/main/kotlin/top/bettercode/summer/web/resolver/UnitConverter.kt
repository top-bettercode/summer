package top.bettercode.summer.web.resolver

import org.springframework.core.convert.TypeDescriptor
import org.springframework.core.convert.converter.ConditionalGenericConverter
import org.springframework.core.convert.converter.GenericConverter
import top.bettercode.summer.web.support.ApplicationContextHolder
import java.math.BigDecimal
import java.math.RoundingMode
import kotlin.math.log10

/**
 * 数量单位转换
 */
class UnitConverter : ConditionalGenericConverter {

    override fun matches(sourceType: TypeDescriptor, targetType: TypeDescriptor): Boolean {
        return targetType.hasAnnotation(Unit::class.java)
    }

    override fun getConvertibleTypes(): Set<GenericConverter.ConvertiblePair> {
        return setOf(
                GenericConverter.ConvertiblePair(String::class.java, BigDecimal::class.java),
                GenericConverter.ConvertiblePair(String::class.java, Long::class.java),
                GenericConverter.ConvertiblePair(String::class.java, Long::class.javaObjectType),
                GenericConverter.ConvertiblePair(String::class.java, Long::class.javaPrimitiveType!!),
                GenericConverter.ConvertiblePair(String::class.java, Int::class.java),
                GenericConverter.ConvertiblePair(String::class.java, Int::class.javaObjectType),
                GenericConverter.ConvertiblePair(String::class.java, Int::class.javaPrimitiveType!!),
        )
    }

    override fun convert(`object`: Any?, sourceType: TypeDescriptor, targetType: TypeDescriptor): Any? {
        return if ((`object` as String).isBlank()) {
            null
        } else {
            val unit = targetType.getAnnotation(Unit::class.java)!!
            smaller(number = `object`.toString(), type = targetType.type, value = unit.value, scale = unit.scale)
        }
    }

    companion object {

        @JvmStatic
        @JvmOverloads
        fun smaller(number: Number, value: Int = 100, scale: Int = log10(value.toDouble()).toInt()): Long {
            return smaller(number = number, type = Long::class.java, value = value, scale = scale)
        }

        @JvmStatic
        @JvmOverloads
        fun <T> smaller(number: Number, type: Class<T>, value: Int = 100, scale: Int = log10(value.toDouble()).toInt()): T {
            return smaller(number = number.toString(), type = type, value = value, scale = scale)
        }

        @JvmStatic
        @JvmOverloads
        fun <T> smaller(number: String, type: Class<T>, value: Int = 100, scale: Int = log10(value.toDouble()).toInt()): T {
            val result = BigDecimal(number).multiply(BigDecimal(value)).setScale(scale, RoundingMode.HALF_UP)
            return ApplicationContextHolder.conversionService.convert(result, type)!!
        }


        @JvmStatic
        @JvmOverloads
        fun larger(number: Number, value: Int = 100, scale: Int = log10(value.toDouble()).toInt()): BigDecimal {
            return larger(number = number, type = BigDecimal::class.java, value = value, scale = scale)
        }

        @JvmStatic
        @JvmOverloads
        fun <T> larger(number: Number, type: Class<T>, value: Int = 100, scale: Int = log10(value.toDouble()).toInt()): T {
            val result = BigDecimal(number.toString()).divide(BigDecimal(value), scale, RoundingMode.HALF_UP)
            return if (type == BigDecimal::class.java) {
                @Suppress("UNCHECKED_CAST")
                result as T
            } else {
                ApplicationContextHolder.conversionService.convert(result, type)!!
            }
        }
    }
}
