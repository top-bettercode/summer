package top.bettercode.summer.tools.excel.write

import javassist.bytecode.SignatureAttribute
import org.springframework.util.ClassUtils
import top.bettercode.summer.tools.excel.Converter
import top.bettercode.summer.tools.excel.ExcelException
import top.bettercode.summer.tools.excel.PoiExcel
import top.bettercode.summer.tools.excel.write.style.Alignment
import top.bettercode.summer.tools.excel.write.style.CellStyle
import top.bettercode.summer.tools.lang.CharSequenceExtensions.decapitalized
import top.bettercode.summer.tools.lang.util.TimeUtil
import java.lang.invoke.SerializedLambda
import java.math.BigDecimal
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.*
import java.util.function.BiConsumer
import kotlin.jvm.internal.PropertyReference
import kotlin.reflect.jvm.javaType

/**
 *
 * @author Peter Wu
 */
open class CellSetter<E, P>(
    /**
     * 字段标题
     */
    val title: String,
    /**
     * cell 样式
     */
    val style: CellStyle = CellStyle(),
) {

    /**
     * 字段批注
     */
    var comment: String = ""

    /**
     * 列宽度，-1表示自动计算
     */
    var width: Double = -1.0

    /**
     * 行高
     */
    var height: Double = -1.0

    /**
     * 有效数据范围“,”分隔
     */
    var dataValidation: Array<out String> = emptyArray()

    /**
     * cell 样式自定义
     */
    var styleSetter: BiConsumer<CellStyle, E>? = null

    /**
     * 判断是否合并之前相同mergeGetter值的行
     */
    private var mergeGetter: ((E) -> Any?)? = null

    //--------------------------------------------

    /**
     * 是否需要合并
     */
    val needMerge: Boolean by lazy {
        mergeGetter != null
    }

    /**
     * 依赖POI
     */
    val needPOI: Boolean by lazy {
        this is PropertyCellSetter<E, *> && this.setter != null
    }

    /**
     * 设为需要合并
     *
     * @param mergeGetter 以此获取的值为合并依据，连续相同的值自动合并
     * @return ExcelField
     */
    fun mergeBy(mergeGetter: (E) -> Any?): CellSetter<E, P> {
        this.mergeGetter = mergeGetter
        return this
    }

    //--------------------------------------------
    /**
     * @param obj 实体对象
     * @return 单元格值
     */
    fun mergeId(obj: E): Any? {
        return mergeGetter?.invoke(obj)
    }

    //--------------------------------------------

    fun comment(comment: String): CellSetter<E, P> {
        this.comment = comment
        return this
    }

    fun dataValidation(vararg dataValidation: String): CellSetter<E, P> {
        this.dataValidation = dataValidation
        return this
    }

    fun style(styleSetter: BiConsumer<CellStyle, E>?): CellSetter<E, P> {
        this.styleSetter = styleSetter
        return this
    }

    /**
     * @param format 格式 [说明...](https://learn.microsoft.com/en-us/dotnet/api/documentformat.openxml.spreadsheet.numberingformat?view=openxml-2.8.1)
     * 示例：yyyy年mm月d日 hh时mm分ss秒
     * @return this
     */
    fun format(format: String): CellSetter<E, P> {
        this.style.format(format)
        return this
    }

    /**
     * 导出字段水平对齐方式
     *
     *
     * Define horizontal alignment. [here](https://msdn.microsoft.com/en-us/library/documentformat.openxml.spreadsheet.horizontalalignmentvalues(v=office.14).aspx).
     */
    fun align(align: Alignment): CellSetter<E, P> {
        this.style.horizontalAlignment(align.value)
        return this
    }

    fun wrapText(wrapText: Boolean): CellSetter<E, P> {
        this.style.wrapText(wrapText)
        return this
    }

    fun width(width: Double): CellSetter<E, P> {
        this.width = width
        return this
    }

    fun height(height: Double): CellSetter<E, P> {
        this.height = height
        return this
    }

    /**
     * @param obj 实体对象
     * @return 单元格值
     */
    open fun toCell(obj: E): Any? {
        return null
    }

    //--------------------------------------------
    companion object {

        @JvmStatic
        fun <E, P> index(title: String): IndexSetter<E, P> {
            return IndexSetter(title)
        }

        @JvmStatic
        fun <E, P> formula(title: String, expression: String): FormulaSetter<E, P> {
            return FormulaSetter(title, expression)
        }

        @JvmStatic
        fun <E, P> raw(title: String, expression: String): RawSetter<E, P> {
            return RawSetter(title, expression)
        }

        @JvmStatic
        fun <E, P> of(title: String, propertyGetter: Converter<E, P?>): PropertyCellSetter<E, P> {
            val (propertyName: String?, propertyType: Class<P>) = parsePropertyGetter(
                title,
                propertyGetter
            )
            val isDate =
                propertyType == LocalDate::class.java || propertyType == LocalDateTime::class.java || propertyType == Date::class.java
            return PropertyCellSetter(
                title = title,
                propertyName = propertyName,
                propertyType = propertyType,
                propertyGetter = propertyGetter,
                isDate = isDate,
                style = CellStyle(CellStyle.defaultFormat(propertyType))
            )
        }

        @JvmStatic
        fun <E, P> image(
            title: String,
            propertyGetter: Converter<E, P?>
        ): PropertyCellSetter<E, P> {
            return of(title, propertyGetter).setter(PoiExcel.imageSetter)
        }

        //--------------------------------------------
        private fun <E, P> parsePropertyGetter(
            title: String,
            propertyGetter: Converter<E, P?>,
        ): Pair<String?, Class<P>> {
            var propertyType: Class<P>? = null
            var propertyName: String? = null
            try {
                val javaClass = propertyGetter::class.java
                val declaredFields = javaClass.declaredFields
                if (declaredFields.isNotEmpty()) {
                    try {
                        val field = javaClass.getDeclaredField("function")
                        field.isAccessible = true
                        val get = field.get(propertyGetter)
                        if (get is PropertyReference) {
                            @Suppress("UNCHECKED_CAST")
                            propertyType = get.returnType.javaType as Class<P>
                            propertyName = get.name
                        }
                    } catch (_: NoSuchFieldException) {
                    }
                }
                if (propertyType == null) {
                    val writeReplace = javaClass.getDeclaredMethod("writeReplace")
                    writeReplace.isAccessible = true
                    val serializedLambda = writeReplace.invoke(propertyGetter) as SerializedLambda
                    val implMethodName = serializedLambda.implMethodName
                    val methodSignature =
                        SignatureAttribute.toMethodSignature(serializedLambda.instantiatedMethodType)
                    @Suppress("UNCHECKED_CAST")
                    propertyType =
                        ClassUtils.forName(
                            methodSignature.returnType.jvmTypeName(),
                            null
                        ) as Class<P>
                    propertyName = resolvePropertyName(implMethodName)
                    //$lamda-0
                    if (propertyName.contains("lambda\$new$") || propertyName.contains("\$lambda")) {
                        propertyName = null
                    }
                }
            } catch (e: Exception) {
                throw ExcelException(title + "属性解析错误", e)
            }
            return propertyName to propertyType
        }

        private fun resolvePropertyName(methodName: String): String {
            var name = methodName
            if (name.startsWith("get")) {
                name = name.substring(3)
            } else if (name.startsWith("is")) {
                name = name.substring(2)
            }
            return name.decapitalized()
        }

        //--------------------------------------------

        @JvmStatic
        fun <P : Any> toCell(
            property: P,
            isDate: Boolean = false,
            converter: ((P) -> Any?)? = null,
        ): Any? {
            val propertyType = property::class.java
            return if (converter == null) {
                convert(propertyType, property, isDate)
            } else {
                converter(property)
            }
        }

        private fun <P : Any> convert(propertyType: Class<out P>, value: P, isDate: Boolean): Any {
            return when {
                propertyType == String::class.java
                        || propertyType == Date::class.java
                        || propertyType == LocalDate::class.java
                        || propertyType == LocalDateTime::class.java
                    -> {
                    value
                }

                propertyType == Boolean::class.java
                        || propertyType == Boolean::class.javaObjectType -> {
                    if (value as Boolean) "是" else "否"
                }

                isDate && (propertyType == Long::class.javaObjectType
                        || propertyType == Long::class.java) -> {
                    TimeUtil.of((value as Long)).toDate()
                }

                propertyType == BigDecimal::class.java -> {
                    (value as BigDecimal).toDouble()
                }

                ClassUtils.isPrimitiveOrWrapper(propertyType) -> {
                    value
                }

                propertyType.isArray -> {
                    (value as Array<*>).joinToString(",")
                }

                MutableCollection::class.java.isAssignableFrom(propertyType) -> {
                    (value as Collection<*>).joinToString(",")
                }

                else -> {
                    value.toString()
                }
            }
        }

    }

}