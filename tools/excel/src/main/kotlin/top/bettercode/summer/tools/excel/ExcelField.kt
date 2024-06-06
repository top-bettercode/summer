package top.bettercode.summer.tools.excel

import javassist.bytecode.SignatureAttribute
import org.springframework.util.Assert
import org.springframework.util.ClassUtils
import org.springframework.util.ReflectionUtils
import top.bettercode.summer.tools.lang.capitalized
import top.bettercode.summer.tools.lang.decapitalized
import top.bettercode.summer.tools.lang.util.BooleanUtil.toBoolean
import top.bettercode.summer.tools.lang.util.TimeUtil.Companion.of
import top.bettercode.summer.web.resolver.UnitConverter
import top.bettercode.summer.web.support.code.CodeServiceHolder
import java.io.Serializable
import java.lang.invoke.SerializedLambda
import java.lang.reflect.Method
import java.math.BigDecimal
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.*
import java.util.function.BiConsumer
import java.util.function.Consumer
import javax.validation.ConstraintViolationException
import javax.validation.Validator
import kotlin.collections.set
import kotlin.jvm.internal.PropertyReference
import kotlin.math.log10
import kotlin.reflect.KClass
import kotlin.reflect.jvm.javaType

/**
 * Excel字段描述
 *
 * @param <P> 属性类型
 * @param <T> 实体类型
</T></P> */
class ExcelField<T, P : Any?> {
    /**
     * 导出字段标题
     */
    val title: String

    /**
     * 序号字段
     */
    val isIndexColumn: Boolean

    /**
     * poi字段设置方法
     */
    var cellSetter: ((PoiExcel, ExcelFieldCell<T>) -> Unit)? = null

    /**
     * 公式字段
     */
    val isFormula: Boolean

    /**
     * 导出字段批注
     */
    var comment = ""

    /**
     * 有效数据范围“,”分隔
     */
    var dataValidation: Array<out String> = emptyArray()

    private val defaultFormat: String? by lazy {
        when (propertyType) {
            Int::class.javaObjectType, Int::class.java, Int::class.java -> {
                "0"
            }

            Long::class.javaObjectType, Long::class.java, Long::class.java -> {
                "0"
            }

            Double::class.javaObjectType, Double::class.java, Double::class.java -> {
                "0.00"
            }

            Float::class.javaObjectType, Float::class.java, Float::class.java -> {
                "0.00"
            }

            LocalDate::class.java -> {
                ExcelFieldCell.DEFAULT_DATE_FORMAT
            }

            Date::class.java, LocalDateTime::class.java -> {
                ExcelFieldCell.DEFAULT_DATE_TIME_FORMAT
            }

            else -> {
                ExcelFieldCell.DEFAULT_FORMAT
            }
        }
    }

    /**
     * cell 样式
     */
    val cellStyle: CellStyle = CellStyle()

    /**
     * cell 样式自定义
     */
    var styleSetter: BiConsumer<CellStyle, T>? = null

    /**
     * 列宽度，-1表示自动计算
     */
    var width = -1.0

    /**
     * 行高
     */
    var height = -1.0

    /**
     * 默认值
     */
    private var defaultValue: P? = null

    /**
     * 默认空值
     */
    private var nullValue = ""

    /**
     * 是否需要合并
     */
    val isMerge: Boolean by lazy {
        mergeGetter != null
    }

    /**
     * 判断是否合并之前相同mergeGetter值的行
     */
    private var mergeGetter: ((T) -> Any?)? = null


    /**
     * 获取实体属性
     */
    private lateinit var propertyGetter: ExcelConverter<T, P?>

    /**
     * 设置实体属性
     */
    private var propertySetter: ExcelCellSetter<T, P?>? = null

    /**
     * 单元格值转属性字段值
     */
    private var propertyConverter: ((Any) -> P?)? = null

    /**
     * 默认单元格值转属性字段值
     */
    private val defaultPropertyConverter: (Any) -> P? by lazy {
        { cellValue: Any? ->
            @Suppress("UNCHECKED_CAST")
            when (propertyType) {
                String::class.java -> {
                    cellValue.toString()
                }

                Boolean::class.javaObjectType, Boolean::class.java, Boolean::class.java -> {
                    cellValue as? Boolean ?: toBoolean(cellValue.toString())
                }

                Int::class.javaObjectType, Int::class.java, Int::class.java -> {
                    if (cellValue is String) {
                        BigDecimal(cellValue).toInt()
                    } else (cellValue as BigDecimal).toInt()
                }

                Long::class.javaObjectType, Long::class.java, Long::class.java -> when {
                    isDateField -> {
                        when (cellValue) {
                            is LocalDateTime -> {
                                of(cellValue).toMillis()
                            }

                            else -> {
                                throw ExcelException("转换为毫秒数失败")
                            }
                        }
                    }

                    else -> {
                        when (cellValue) {
                            is String -> {
                                BigDecimal(cellValue).toLong()
                            }

                            else -> (cellValue as BigDecimal).toLong()
                        }
                    }
                }

                BigDecimal::class.java -> {
                    when (cellValue) {
                        is String -> {
                            BigDecimal(cellValue)
                        }

                        else -> cellValue
                    }
                }

                Double::class.javaObjectType, Double::class.java, Double::class.java -> {
                    when (cellValue) {
                        is String -> {
                            BigDecimal(cellValue).toDouble()
                        }

                        else -> (cellValue as BigDecimal).toDouble()
                    }
                }

                Float::class.javaObjectType, Float::class.java, Float::class.java -> {
                    when (cellValue) {
                        is String -> {
                            BigDecimal(cellValue).toFloat()
                        }

                        else -> (cellValue as BigDecimal).toFloat()
                    }
                }

                Date::class.java -> {
                    when (cellValue) {
                        is LocalDateTime -> {
                            of(cellValue).toDate()
                        }

                        else -> {
                            throw ExcelException("转换为时间失败")
                        }
                    }
                }

                LocalDateTime::class.java -> {
                    when (cellValue) {
                        is LocalDateTime -> {
                            cellValue
                        }

                        else -> {
                            throw ExcelException("转换为时间失败")
                        }
                    }
                }

                LocalDate::class.java -> {
                    if (cellValue is LocalDateTime) {
                        cellValue.toLocalDate()
                    } else {
                        throw ExcelException("转换为日期失败")
                    }
                }

                else -> throw IllegalArgumentException("不支持的数据类型:" + propertyType!!.name)
            } as P?
        }
    }

    /**
     * 属性字段值验证
     */
    var validator: Consumer<T>? = null

    /**
     * 属性字段值转单元格值
     */
    private var cellConverter: ((P) -> Any?)? = null

    /**
     * 默认属性字段值转单元格值
     */
    private val defaultCellConverter: (P) -> Any? by lazy {
        { property: P ->
            when {
                propertyType == String::class.java || propertyType == Date::class.java -> {
                    property
                }

                propertyType == Boolean::class.java || propertyType == Boolean::class.javaObjectType || propertyType == Boolean::class.java -> {
                    if (property as Boolean) "是" else "否"
                }

                propertyType == LocalDate::class.java -> {
                    property
                }

                propertyType == LocalDateTime::class.java -> {
                    property
                }

                isDateField && (propertyType == Long::class.javaObjectType || propertyType == Long::class.java || propertyType == Long::class.java) -> {
                    of((property as Long)).toDate()
                }

                propertyType != null && ClassUtils.isPrimitiveOrWrapper(propertyType!!) -> {
                    property
                }

                propertyType == BigDecimal::class.java -> {
                    (property as BigDecimal).toDouble()
                }

                propertyType!!.isArray -> {
                    val length = java.lang.reflect.Array.getLength(property)
                    val buffer = StringBuilder()
                    for (i in 0 until length) {
                        if (i > 0) {
                            buffer.append(",")
                        }
                        buffer.append(java.lang.reflect.Array.get(property, i))
                    }
                    buffer.toString()
                }

                propertyType != null && MutableCollection::class.java.isAssignableFrom(propertyType!!) -> {
                    (property as Collection<*>).joinToString(",")
                }

                cellSetter != null -> {
                    property
                }

                else -> {
                    property.toString()
                }
            }
        }
    }

    /**
     * 实体类型
     */
    var entityType: Class<T>? = null

    /**
     * 属性字段类型
     */
    var propertyType: Class<P>? = null

    /**
     * 属性字段名称
     */
    private var propertyName: String? = null

    /**
     * 是否时间日期字段
     */
    var isDateField: Boolean = false

    private val writeMethod: Method by lazy {
        try {
            entityType!!.getMethod("set" + propertyName!!.capitalized(), propertyType)
        } catch (e: NoSuchMethodException) {
            if (ClassUtils.isPrimitiveWrapper(propertyType!!)) {
                @Suppress("UNCHECKED_CAST")
                propertyType = primitiveWrapperTypeMap[propertyType!!] as Class<P>
                entityType!!.getMethod("set" + propertyName!!.capitalized(), propertyType)
            } else {
                throw e
            }
        }
    }

    //--------------------------------------------
    @JvmOverloads
    fun scale(scale: Int = 2): ExcelField<T, P> {
        return unit(1, scale)
    }

    @JvmOverloads
    fun yuan(scale: Int = 2): ExcelField<T, P> {
        return unit(100, scale)
    }

    @JvmOverloads
    fun unit(value: Int, scale: Int = log10(value.toDouble()).toInt()): ExcelField<T, P> {
        return cell { property: P ->
            val result =
                UnitConverter.larger(number = property as Number, value = value, scale = scale)
            result.toDouble()
        }
            .property { cell: Any ->
                UnitConverter.smaller(
                    number = BigDecimal(cell.toString()),
                    propertyType!!,
                    value = value,
                    scale = scale
                )
            }
    }

    /**
     * @param format 格式 [说明...](https://learn.microsoft.com/en-us/dotnet/api/documentformat.openxml.spreadsheet.numberingformat?view=openxml-2.8.1)
     * 示例：yyyy年m月d日 hh时mm分ss秒
     * @return this
     */
    @JvmOverloads
    fun date(format: String = ExcelFieldCell.DEFAULT_DATE_TIME_FORMAT): ExcelField<T, P> {
        this.cellStyle.format(format)
        isDateField = true
        return this
    }

    fun code(): ExcelField<T, P> {
        Assert.hasText(propertyName, "属性名称未设置")
        return code(propertyName!!)
    }

    fun codeServiceRef(codeServiceRef: String): ExcelField<T, P> {
        Assert.hasText(propertyName, "属性名称未设置")
        return code(codeServiceRef, propertyName!!)
    }

    fun code(codeType: String): ExcelField<T, P> {
        return code("", codeType)
    }

    fun code(codeServiceRef: String, codeType: String): ExcelField<T, P> {
        return cell { property: P ->
            val codeService = CodeServiceHolder[codeServiceRef]
            if (property is String) {
                val separator = ","
                if (property.contains(separator)) {
                    val split =
                        property.split(separator).filter { it.isNotBlank() }.map { it.trim() }
                    return@cell split.joinToString(separator) { s: String ->
                        codeService.getDicCodes(
                            codeType
                        )!!.getName(s)
                    }
                } else {
                    return@cell codeService.getDicCodes(codeType)!!.getName(property)
                }
            } else {
                return@cell codeService.getDicCodes(codeType)!!.getName((property as Serializable))
            }
        }.property { cellValue: Any ->
            val codeService = CodeServiceHolder[codeServiceRef]
            val separator = ","
            val value = cellValue.toString()
            @Suppress("UNCHECKED_CAST")
            return@property if (value.contains(separator)) {
                val split = value.split(separator).filter { it.isNotBlank() }.map { it.trim() }

                split.joinToString(separator) { s: String ->
                    codeService.getDicCodes(codeType)!!.getCode(s)?.toString()
                        ?: throw IllegalArgumentException("无\"$s\"对应的类型")
                }
            } else {
                codeService.getDicCodes(codeType)!!.getCode(value)
                    ?: throw IllegalArgumentException("无\"$cellValue\"对应的类型")
            } as P?
        }
    }


    //--------------------------------------------
    fun getter(propertyGetter: ExcelConverter<T, P?>): ExcelField<T, P> {
        this.propertyGetter = propertyGetter
        return this
    }

    fun setter(propertySetter: ExcelCellSetter<T, P?>): ExcelField<T, P> {
        this.propertySetter = propertySetter
        return this
    }

    /**
     * 单元格值转属性字段值
     */
    fun property(propertyConverter: (Any) -> P?): ExcelField<T, P> {
        this.propertyConverter = propertyConverter
        return this
    }

    fun cellSetter(cellSetter: (PoiExcel, ExcelFieldCell<T>) -> Unit): ExcelField<T, P> {
        this.cellSetter = cellSetter
        return this
    }

    fun validator(validator: Consumer<T>): ExcelField<T, P> {
        this.validator = validator
        return this
    }

    /**
     * 属性字段值转单元格值
     */
    fun cell(cellConverter: (P) -> Any?): ExcelField<T, P> {
        this.cellConverter = cellConverter
        return this
    }

    fun none(nullValue: String): ExcelField<T, P> {
        this.nullValue = nullValue
        return this
    }

    @Suppress("UNCHECKED_CAST")
    constructor(title: String, propertyGetter: ExcelConverter<T, P?>) {
        this.title = title
        this.propertyGetter = propertyGetter
        try {
            val javaClass = propertyGetter::class.java
            val declaredFields = javaClass.declaredFields
            if (declaredFields.isNotEmpty()) {
                try {
                    val field = javaClass.getDeclaredField("function")
                    field.isAccessible = true
                    val get = field.get(propertyGetter)
                    if (get is PropertyReference) {
                        propertyType = get.returnType.javaType as Class<P>
                        entityType = (get.owner as KClass<*>).java as Class<T>
                        propertyName = get.name
                        propertySetter = ExcelCellSetter { entity, property ->
                            ReflectionUtils.invokeMethod(writeMethod, entity, property)
                        }
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
                propertyType =
                    ClassUtils.forName(methodSignature.returnType.jvmTypeName(), null) as Class<P>
                propertyName = resolvePropertyName(implMethodName)
                entityType = ClassUtils.forName(
                    methodSignature.parameterTypes[0].jvmTypeName(),
                    null
                ) as Class<T>
                //$lamda-0
                if (!propertyName!!.contains("lambda\$new$") && !propertyName!!.contains("\$lambda")) {
                    propertySetter = ExcelCellSetter { entity, property ->
                        ReflectionUtils.invokeMethod(writeMethod, entity, property)
                    }
                } else {
                    propertyName = null
                }
            }
        } catch (e: Exception) {
            throw ExcelException(title + "属性解析错误", e)
        }
        isIndexColumn = false
        isFormula = false

        Assert.notNull(propertyType, "propertyType 不能为空")

        isDateField =
            propertyType == LocalDate::class.java || propertyType == LocalDateTime::class.java || propertyType == Date::class.java

        this.cellStyle.defaultValueFormatting = defaultFormat
    }

    constructor(title: String, indexColumn: Boolean, formula: Boolean) {
        this.title = title
        this.isIndexColumn = indexColumn
        this.isFormula = formula
        this.cellStyle.format(ExcelFieldCell.DEFAULT_FORMAT)
    }

    @JvmOverloads
    constructor(
        title: String,
        propertyType: Class<P>,
        propertyGetter: ExcelConverter<T, P?>,
        isIndexColumn: Boolean = false,
        isFormula: Boolean = false
    ) {
        this.title = title
        this.propertyType = propertyType
        this.propertyGetter = propertyGetter
        this.isIndexColumn = isIndexColumn
        this.isFormula = isFormula

        Assert.notNull(propertyType, "propertyType 不能为空")

        isDateField =
            propertyType == LocalDate::class.java || propertyType == LocalDateTime::class.java || propertyType == Date::class.java

        this.cellStyle.defaultValueFormatting = defaultFormat
    }

    //--------------------------------------------
    fun propertyName(propertyName: String?): ExcelField<T, P> {
        this.propertyName = propertyName
        return this
    }

    fun comment(comment: String): ExcelField<T, P> {
        this.comment = comment
        return this
    }

    fun dataValidation(vararg dataValidation: String): ExcelField<T, P> {
        this.dataValidation = dataValidation
        return this
    }

    fun defaultValue(defaultValue: P): ExcelField<T, P> {
        this.defaultValue = defaultValue
        return this
    }

    fun style(styleSetter: BiConsumer<CellStyle, T>?): ExcelField<T, P> {
        this.styleSetter = styleSetter
        return this
    }

    /**
     * @param format 格式 [说明...](https://learn.microsoft.com/en-us/dotnet/api/documentformat.openxml.spreadsheet.numberingformat?view=openxml-2.8.1)
     * 示例：yyyy年m月d日 hh时mm分ss秒
     * @return this
     */
    fun format(format: String?): ExcelField<T, P> {
        this.cellStyle.format(format)
        return this
    }

    /**
     * 导出字段水平对齐方式
     *
     *
     * Define horizontal alignment. [here](https://msdn.microsoft.com/en-us/library/documentformat.openxml.spreadsheet.horizontalalignmentvalues(v=office.14).aspx).
     */
    fun align(align: Alignment): ExcelField<T, P> {
        this.cellStyle.horizontalAlignment(align.value)
        return this
    }

    fun wrapText(wrapText: Boolean): ExcelField<T, P> {
        this.cellStyle.wrapText(wrapText)
        return this
    }

    fun width(width: Double): ExcelField<T, P> {
        this.width = width
        return this
    }

    fun height(height: Double): ExcelField<T, P> {
        this.height = height
        return this
    }

    /**
     * 设为需要合并
     *
     * @param mergeGetter 以此获取的值为合并依据，连续相同的值自动合并
     * @return ExcelField
     */
    fun mergeBy(mergeGetter: (T) -> Any?): ExcelField<T, P> {
        this.mergeGetter = mergeGetter
        return this
    }
    //--------------------------------------------
    /**
     * @param obj 实体对象
     * @return 单元格值
     */
    fun getMergeId(obj: T): Any? {
        return mergeGetter!!(obj)
    }

    /**
     * @param obj 实体对象
     * @return 单元格值
     */
    fun toCellValue(obj: T): Any? {
        return if (isFormula) {
            nullValue
        } else {
            var property = propertyGetter.convert(obj)
            if (property == null) {
                property = defaultValue
            }
            if (property == null) {
                nullValue
            } else {
                (cellConverter ?: defaultCellConverter)(property)
            }
        }
    }

    /**
     * @param obj            实体对象
     * @param cellValue      单元格值
     * @param validator      参数验证
     * @param validateGroups 参数验证组
     */
    fun setProperty(
        obj: T,
        cellValue: Any?,
        validator: Validator,
        validateGroups: Array<Class<*>>
    ) {
        val property: P? = if (isEmptyCell(cellValue)) {
            defaultValue
        } else {
            (propertyConverter ?: defaultPropertyConverter)(cellValue!!)
        }
        propertySetter?.let { it[obj] = property }
        if (propertyName != null) {
            val constraintViolations =
                validator.validateProperty<Any>(obj, propertyName, *validateGroups)
            if (constraintViolations.isNotEmpty()) {
                throw ConstraintViolationException(constraintViolations)
            }
        }
    }

    fun isEmptyCell(cellValue: Any?): Boolean {
        return cellValue == null || cellValue is CharSequence && (cellValue as CharSequence?).isNullOrBlank()
    }

    //--------------------------------------------

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
    companion object {
        //--------------------------------------------
        @JvmStatic
        fun <T, P> index(title: String): ExcelField<T, P> {
            return ExcelField(title, indexColumn = true, formula = false)
        }

        @JvmStatic
        fun <T, P> formula(title: String, expression: String): ExcelField<T, P> {
            val excelField = ExcelField<T, P>(title, indexColumn = false, formula = true)
            excelField.nullValue = expression
            return excelField
        }

        /**
         * 支持导出(如果 propertyGetter 符合 ClassName::getProperty Lambda 签名，则可自动识别setter，支持导入)的初始化方法
         *
         * @param <P>            属性类型
         * @param <T>            实体类型
         * @param title          标题
         * @param propertyGetter 属性获取方法
         * @return Excel字段描述
        </T></P> */
        @JvmStatic
        fun <T, P> of(title: String, propertyGetter: ExcelConverter<T, P?>): ExcelField<T, P> {
            return ExcelField(title, propertyGetter)
        }

        @JvmStatic
        @JvmOverloads
        fun <T, P> of(
            title: String,
            propertyType: Class<P>,
            propertyGetter: ExcelConverter<T, P?>,
            isIndexColumn: Boolean = false,
            isFormula: Boolean = false
        ): ExcelField<T, P> {
            return ExcelField(title, propertyType, propertyGetter, isIndexColumn, isFormula)
        }

        @JvmStatic
        fun <T, P> poi(
            title: String,
            propertyGetter: ExcelConverter<T, P?>,
            cellSetter: (PoiExcel, ExcelFieldCell<T>) -> Unit
        ): ExcelField<T, P> {
            return ExcelField(title, propertyGetter).cellSetter(cellSetter)
        }

        @JvmStatic
        fun <T, P> image(title: String, propertyGetter: ExcelConverter<T, P?>): ExcelField<T, P> {
            return ExcelField(title, propertyGetter).cellSetter(PoiExcel.imageSetter)
        }

        //--------------------------------------------
        val primitiveWrapperTypeMap: MutableMap<Class<*>, Class<*>> = IdentityHashMap(8)

        init {
            primitiveWrapperTypeMap[Boolean::class.javaObjectType] = Boolean::class.java
            primitiveWrapperTypeMap[Byte::class.javaObjectType] = Byte::class.java
            primitiveWrapperTypeMap[Char::class.javaObjectType] = Char::class.java
            primitiveWrapperTypeMap[Double::class.javaObjectType] = Double::class.java
            primitiveWrapperTypeMap[Float::class.javaObjectType] = Float::class.java
            primitiveWrapperTypeMap[Int::class.javaObjectType] = Int::class.java
            primitiveWrapperTypeMap[Long::class.javaObjectType] = Long::class.java
            primitiveWrapperTypeMap[Short::class.javaObjectType] = Short::class.java
            primitiveWrapperTypeMap[Void::class.javaObjectType] = Void.TYPE
        }
    }
}
