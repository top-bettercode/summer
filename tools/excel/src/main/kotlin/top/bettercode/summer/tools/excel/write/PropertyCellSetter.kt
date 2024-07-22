package top.bettercode.summer.tools.excel.write

import org.springframework.util.Assert
import top.bettercode.summer.tools.excel.Converter
import top.bettercode.summer.tools.excel.PoiExcel
import top.bettercode.summer.tools.excel.write.style.CellStyle
import top.bettercode.summer.tools.excel.write.style.CellStyle.Companion.DEFAULT_DATE_TIME_FORMAT
import top.bettercode.summer.web.resolver.UnitConverter
import top.bettercode.summer.web.support.code.CodeServiceHolder
import java.io.Serializable
import java.math.BigDecimal
import java.math.BigInteger
import kotlin.math.log10

/**
 * Excel字段描述
 *
 * @param <P> 属性类型
 * @param <T> 实体类型
</T></P> */
class PropertyCellSetter<E, P>(
    /**
     * 字段标题
     */
    title: String,
    /**
     * 属性字段名称
     */
    private val propertyName: String?,

    /**
     * 属性字段类型
     */
    private val propertyType: Class<P>,

    /**
     * 获取实体属性
     */
    private val propertyGetter: Converter<E, P?>,

    /**
     * 是否时间日期字段
     */
    var isDate: Boolean,
    /**
     * cell 样式
     */
    style: CellStyle,
) : CellSetter<E, P>(title = title, style = style) {

    /**
     * 默认值
     */
    private var defaultValue: P? = null

    /**
     * 属性字段值转单元格值
     */
    private var converter: ((P) -> Any?)? = null

    /**
     * poi字段设置方法
     */
    var setter: ((PoiExcel, CellData<E>) -> Unit)? = null

    //--------------------------------------------

    @JvmOverloads
    fun scale(scale: Int = 2): PropertyCellSetter<E, P> {
        return unit(1, scale)
    }

    @JvmOverloads
    fun yuan(scale: Int = 2): PropertyCellSetter<E, P> {
        return unit(100, scale)
    }

    @JvmOverloads
    fun unit(value: Int, scale: Int = log10(value.toDouble()).toInt()): PropertyCellSetter<E, P> {
        return converter { property: P ->
            val result =
                UnitConverter.larger(number = property as Number, value = value, scale = scale)
            result.toDouble()
        }
    }

    fun code(): PropertyCellSetter<E, P> {
        Assert.hasText(propertyName, "属性名称未设置")
        return code(propertyName!!)
    }

    fun codeServiceRef(codeServiceRef: String): PropertyCellSetter<E, P> {
        Assert.hasText(propertyName, "属性名称未设置")
        return code(codeServiceRef, propertyName!!)
    }

    fun code(codeType: String): PropertyCellSetter<E, P> {
        return code("", codeType)
    }

    fun code(codeServiceRef: String, codeType: String): PropertyCellSetter<E, P> {
        return converter { property: P ->
            val dicCodes = CodeServiceHolder.get(codeServiceRef, codeType)
            if (property is String) {
                val separator = ","
                if (property.contains(separator)) {
                    val split =
                        property.split(separator).filter { it.isNotBlank() }.map { it.trim() }
                    return@converter split.joinToString(separator) { s: String ->
                        dicCodes?.getName(s) ?: s
                    }
                } else {
                    return@converter dicCodes?.getName(property) ?: property
                }
            } else {
                val code = property as Serializable
                return@converter dicCodes?.getName(code) ?: code
            }
        }
    }

    fun stripTrailingZeros(): PropertyCellSetter<E, P> {
        converter { property: P ->
            when (property) {
                is BigDecimal -> property
                is Double -> property.toBigDecimal()
                is Float -> property.toBigDecimal()
                is Int -> property.toBigDecimal()
                is Long -> property.toBigDecimal()
                is String -> property.toBigDecimal()
                is BigInteger -> property.toBigDecimal()
                else -> return@converter property
            }.stripTrailingZeros().toPlainString()
        }
        return this
    }

    /**
     * @param format 格式 [说明...](https://learn.microsoft.com/en-us/dotnet/api/documentformat.openxml.spreadsheet.numberingformat?view=openxml-2.8.1)
     * 示例：yyyy年m月d日 hh时mm分ss秒
     * @return this
     */
    @JvmOverloads
    fun date(format: String = DEFAULT_DATE_TIME_FORMAT): PropertyCellSetter<E, P> {
        this.style.format(format)
        this.isDate = true
        return this
    }

    //--------------------------------------------
    /**
     * 属性字段值转单元格值
     */
    fun converter(converter: (P) -> Any?): PropertyCellSetter<E, P> {
        this.converter = converter
        return this
    }

    /**
     * poi 自定义 单元格值
     */
    fun setter(setter: (PoiExcel, CellData<E>) -> Unit): PropertyCellSetter<E, P> {
        this.setter = setter
        return this
    }

    /**
     * 设置默认值
     */
    fun defaultValue(defaultValue: P): PropertyCellSetter<E, P> {
        this.defaultValue = defaultValue
        return this
    }

    /**
     * @param obj 实体对象
     * @return 单元格值
     */
    override fun toCell(obj: E): Any? {
        val property = propertyGetter.convert(obj)
        return when {
            property == null -> {
                defaultValue
            }

            setter != null -> {
                property
            }

            else -> toCell(property = property, isDate = isDate, converter = converter)
        }
    }

}
