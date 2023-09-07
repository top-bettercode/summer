package top.bettercode.summer.tools.excel

import javassist.bytecode.BadBytecode
import javassist.bytecode.SignatureAttribute
import org.slf4j.LoggerFactory
import org.springframework.util.Assert
import org.springframework.util.ClassUtils
import org.springframework.util.ReflectionUtils
import org.springframework.util.StringUtils
import top.bettercode.summer.tools.lang.util.BooleanUtil.toBoolean
import top.bettercode.summer.tools.lang.util.MoneyUtil.toCent
import top.bettercode.summer.tools.lang.util.MoneyUtil.toYun
import top.bettercode.summer.tools.lang.util.TimeUtil.Companion.of
import top.bettercode.summer.web.support.code.CodeServiceHolder
import java.io.Serializable
import java.lang.invoke.SerializedLambda
import java.lang.reflect.InvocationTargetException
import java.lang.reflect.Method
import java.math.BigDecimal
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.*
import javax.validation.ConstraintViolationException
import javax.validation.Validator
import kotlin.collections.set
import kotlin.jvm.internal.PropertyReference
import kotlin.reflect.KClass
import kotlin.reflect.jvm.javaType

/**
 * Excel字段描述
 *
 * @param <P> 属性类型
 * @param <T> 实体类型
</T></P> */
open class ExcelField<T, P : Any?> {
    /**
     * 导出字段标题
     */
    val title: String

    /**
     * 导出字段批注
     */
    var comment = ""

    /**
     * 有效数据范围“,”分隔
     */
    var dataValidation: Array<out String> = emptyArray()

    /**
     * 格式 [说明...](https://learn.microsoft.com/en-us/dotnet/api/documentformat.openxml.spreadsheet.numberingformat?view=openxml-2.8.1)
     * 示例：yyyy"年"m"月"d"日" hh"时"mm"分"ss"秒"
     */
    var format: String? = null

    /**
     * 导出字段水平对齐方式
     *
     *
     * Define horizontal alignment. [here](https://msdn.microsoft.com/en-us/library/documentformat.openxml.spreadsheet.horizontalalignmentvalues(v=office.14).aspx).
     */
    var align = Alignment.CENTER

    /**
     * 是否自动换行
     */
    var wrapText = true

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
    var nullValue = ""

    /**
     * 是否需要合并
     */
    var isMerge = false
        private set

    /**
     * 判断是否合并之前相同mergeGetter值的行
     */
    private var mergeGetter: ((T) -> Any?)? = null

    /**
     * 序号字段
     */
    val isIndexColumn: Boolean

    /**
     * 图片字段
     */
    val isImageColumn: Boolean

    /**
     * 公式字段
     */
    val isFormula: Boolean

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
    private lateinit var propertyConverter: (Any) -> P?

    /**
     * 属性字段值验证
     */
    var validator: ((T) -> Unit)? = null

    /**
     * 属性字段值转单元格值
     */
    private lateinit var cellConverter: (P) -> Any?

    /**
     * 实体类型
     */
    var entityType: Class<T>? = null

    /**
     * 属性字段类型
     */
    var propertyType: Class<*>? = null

    /**
     * 属性字段名称
     */
    protected var propertyName: String? = null

    /**
     * 是否时间日期字段
     */
    var isDateField = false
        protected set

    //--------------------------------------------
    @Suppress("UNCHECKED_CAST")
    @JvmOverloads
    fun yuan(scale: Int = 2): ExcelField<T, P> {
        return cell { property: P -> toYun((property as Long), scale).toPlainString() }.property { yun: Any ->
            toCent(yun as BigDecimal) as P
        }
    }

    /**
     * @param format 格式 [说明...](https://learn.microsoft.com/en-us/dotnet/api/documentformat.openxml.spreadsheet.numberingformat?view=openxml-2.8.1)
     * @return this
     */
    @JvmOverloads
    fun date(format: String? = ExcelCell.DEFAULT_DATE_TIME_FORMAT): ExcelField<T, P> {
        this.format = format
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
                val code = property.toString()
                if (code.contains(",")) {
                    val split = code.split(",".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
                    return@cell StringUtils.arrayToCommaDelimitedString(Arrays.stream<String>(split).map<String> { s: String -> codeService.getDicCodes(codeType)!!.getName(s.trim { it <= ' ' }) }.toArray())
                } else {
                    return@cell codeService.getDicCodes(codeType)!!.getName(code)
                }
            } else {
                return@cell codeService.getDicCodes(codeType)!!.getName((property as Serializable))
            }
        }.property { cellValue: Any -> getCode(codeServiceRef, codeType, cellValue.toString()) }
    }

    //--------------------------------------------
    @Suppress("UNCHECKED_CAST")
    private fun getCode(codeServiceRef: String, codeType: String, cellValue: String): P? {
        val codeService = CodeServiceHolder[codeServiceRef]
        return if (cellValue.contains(",")) {
            val split = cellValue.split(",".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
            StringUtils.arrayToCommaDelimitedString(Arrays.stream(split).map { s: String ->
                val code = codeService.getDicCodes(codeType)!!.getCode(s.trim { it <= ' ' })
                        ?: throw IllegalArgumentException("无\"$s\"对应的类型")
                code
            }.toArray()) as P?
        } else {
            val code = codeService.getDicCodes(codeType)!!.getCode(cellValue)
                    ?: throw IllegalArgumentException("无\"$cellValue\"对应的类型")
            code as P?
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

    fun validator(validator: ((T) -> Unit)?): ExcelField<T, P> {
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
    constructor(title: String, propertyGetter: ExcelConverter<T, P?>, indexColumn: Boolean, imageColumn: Boolean) {
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
                        propertyType = get.returnType.javaType as Class<*>
                        entityType = (get.owner as KClass<*>).java as Class<T>
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
                val methodSignature = SignatureAttribute.toMethodSignature(serializedLambda.instantiatedMethodType)
                propertyType = ClassUtils.forName(methodSignature.returnType.jvmTypeName(), null)
                propertyName = resolvePropertyName(implMethodName)
                entityType = ClassUtils.forName(methodSignature.parameterTypes[0].jvmTypeName(), null) as Class<T>
                //$lamda-0
                if (!propertyName!!.contains("lambda\$new$") && !propertyName!!.contains("\$lambda")) {
                    try {
                        var writeMethod: Method
                        try {
                            writeMethod = entityType!!.getMethod("set" + StringUtils.capitalize(propertyName!!), propertyType)
                        } catch (e: NoSuchMethodException) {
                            if (ClassUtils.isPrimitiveWrapper(propertyType!!)) {
                                propertyType = primitiveWrapperTypeMap[propertyType!!]
                                writeMethod = entityType!!.getMethod("set" + StringUtils.capitalize(propertyName!!), propertyType)
                            } else {
                                throw e
                            }
                        }
                        val fWriteMethod = writeMethod
                        propertySetter = ExcelCellSetter { entity, property -> ReflectionUtils.invokeMethod(fWriteMethod, entity, property) }
                    } catch (e: NoSuchMethodException) {
                        val log = LoggerFactory.getLogger(ExcelField::class.java)
                        if (log.isDebugEnabled) {
                            log.debug("自动识别属性{} setter方法失败", propertyName)
                        }
                        propertyName = null
                    }
                } else {
                    propertyName = null
                }
            }
        } catch (e: NoSuchMethodException) {
            throw ExcelException(title + "属性解析错误", e)
        } catch (e: IllegalAccessException) {
            throw ExcelException(title + "属性解析错误", e)
        } catch (e: InvocationTargetException) {
            throw ExcelException(title + "属性解析错误", e)
        } catch (e: ClassNotFoundException) {
            throw ExcelException(title + "属性解析错误", e)
        } catch (e: BadBytecode) {
            throw ExcelException(title + "属性解析错误", e)
        }
        isIndexColumn = indexColumn
        isImageColumn = imageColumn
        isFormula = false
        init()
    }

    /**
     * 只支持导出的初始化方法
     *
     * @param title          标题
     * @param propertyType   属性字段类型
     * @param propertyGetter 属性获取方法
     */
    private constructor(title: String, propertyType: Class<P>, propertyGetter: ExcelConverter<T, P?>, indexColumn: Boolean, imageColumn: Boolean) {
        this.title = title
        this.propertyType = propertyType
        this.propertyGetter = propertyGetter
        isIndexColumn = indexColumn
        isImageColumn = imageColumn
        isFormula = false
        init()
    }

    constructor(title: String, indexColumn: Boolean, imageColumn: Boolean, formula: Boolean) {
        this.title = title
        isIndexColumn = indexColumn
        isImageColumn = imageColumn
        isFormula = formula
        format = ExcelCell.DEFAULT_FORMAT
    }

    @Suppress("UNCHECKED_CAST")
    private fun init() {
        Assert.notNull(propertyType, "propertyType 不能为空")
        if (format == null) {
            when (propertyType) {
                Int::class.javaObjectType, Int::class.javaPrimitiveType, Int::class.java -> {
                    format = "0"
                }

                Long::class.javaObjectType, Long::class.javaPrimitiveType, Long::class.java -> {
                    format = "0"
                }

                Double::class.javaObjectType, Double::class.javaPrimitiveType, Double::class.java -> {
                    format = "0.00"
                }

                Float::class.javaObjectType, Float::class.javaPrimitiveType, Float::class.java -> {
                    format = "0.00"
                }

                LocalDate::class.java -> {
                    isDateField = true
                    format = ExcelCell.DEFAULT_DATE_FORMAT
                }

                Date::class.java, LocalDateTime::class.java -> {
                    isDateField = true
                    format = ExcelCell.DEFAULT_DATE_TIME_FORMAT
                }

                else -> {
                    format = ExcelCell.DEFAULT_FORMAT
                }
            }
        }
        propertyConverter = { cellValue: Any? ->
            when (propertyType) {
                String::class.java -> {
                    cellValue.toString()
                }

                Boolean::class.javaObjectType, Boolean::class.javaPrimitiveType, Boolean::class.java -> {
                    cellValue as? Boolean ?: toBoolean(cellValue.toString())
                }

                Int::class.javaObjectType, Int::class.javaPrimitiveType, Int::class.java -> {
                    if (cellValue is String) {
                        BigDecimal(cellValue).toInt()
                    } else (cellValue as BigDecimal).toInt()
                }

                Long::class.javaObjectType, Long::class.javaPrimitiveType, Long::class.java -> when {
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

                Double::class.javaObjectType, Double::class.javaPrimitiveType, Double::class.java -> {
                    when (cellValue) {
                        is String -> {
                            BigDecimal(cellValue).toDouble()
                        }

                        else -> (cellValue as BigDecimal).toDouble()
                    }
                }

                Float::class.javaObjectType, Float::class.javaPrimitiveType, Float::class.java -> {
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
        cellConverter = { property: P ->
            if (propertyType == String::class.java || propertyType == Date::class.java) {
                property
            } else if (propertyType == Boolean::class.javaPrimitiveType || propertyType == Boolean::class.javaObjectType || propertyType == Boolean::class.java) {
                if (property as Boolean) "是" else "否"
            } else if (propertyType == LocalDate::class.java) {
                property
            } else if (propertyType == LocalDateTime::class.java) {
                property
            } else if (isDateField && (propertyType == Long::class.javaObjectType || propertyType == Long::class.javaPrimitiveType || propertyType == Long::class.java)) {
                of((property as Long)).toDate()
            } else if (propertyType != null && ClassUtils.isPrimitiveOrWrapper(propertyType!!)) {
                property
            } else if (propertyType == BigDecimal::class.java) {
                property
            } else if (propertyType!!.isArray) {
                val length = java.lang.reflect.Array.getLength(property)
                val buffer = StringBuilder()
                for (i in 0 until length) {
                    if (i > 0) {
                        buffer.append(",")
                    }
                    buffer.append(java.lang.reflect.Array.get(property, i))
                }
                buffer.toString()
            } else if (propertyType != null && MutableCollection::class.java.isAssignableFrom(propertyType!!)) {
                StringUtils.collectionToCommaDelimitedString(property as Collection<*>)
            } else if (isImageColumn) {
                property
            } else {
                property.toString()
            }
        }
    }

    //--------------------------------------------
    private fun resolvePropertyName(methodName: String): String {
        var name = methodName
        if (name.startsWith("get")) {
            name = name.substring(3)
        } else if (name.startsWith("is")) {
            name = name.substring(2)
        }
        return StringUtils.uncapitalize(name)
    }

    private fun resolveSetPropertyName(methodName: String): String {
        var name = methodName
        if (name.startsWith("set")) {
            name = name.substring(3)
        }
        return StringUtils.uncapitalize(name)
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

    /**
     * @param format 格式 [说明...](https://learn.microsoft.com/en-us/dotnet/api/documentformat.openxml.spreadsheet.numberingformat?view=openxml-2.8.1)
     * @return this
     */
    fun format(format: String?): ExcelField<T, P> {
        this.format = format
        return this
    }

    fun align(align: Alignment): ExcelField<T, P> {
        this.align = align
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

    fun wrapText(wrapText: Boolean): ExcelField<T, P> {
        this.wrapText = wrapText
        return this
    }

    /**
     * 设为需要合并
     *
     * @param mergeGetter 以此获取的值为合并依据，连续相同的值自动合并
     * @return ExcelField
     */
    fun mergeBy(mergeGetter: (T) -> Any?): ExcelField<T, P> {
        isMerge = true
        this.mergeGetter = mergeGetter
        return this
    }
    //--------------------------------------------
    /**
     * @param obj 实体对象
     * @return 单元格值
     */
    fun mergeId(obj: T): Any? {
        return mergeGetter!!(obj)
    }

    /**
     * @param obj 实体对象
     * @return 单元格值
     */
    fun toCellValue(obj: T): Any? {
        var property = propertyGetter.convert(obj)
        if (property == null) {
            property = defaultValue
        }
        return if (property == null) {
            nullValue
        } else {
            cellConverter(property)
        }
    }

    /**
     * @param obj            实体对象
     * @param cellValue      单元格值
     * @param validator      参数验证
     * @param validateGroups 参数验证组
     */
    fun setProperty(obj: T, cellValue: Any?, validator: Validator, validateGroups: Array<Class<*>>) {
        val property: P? = if (isEmptyCell(cellValue)) {
            defaultValue
        } else {
            propertyConverter(cellValue!!)
        }
        propertySetter?.let { it[obj] = property }
        if (propertyName != null) {
            val constraintViolations = validator.validateProperty<Any>(obj, propertyName, *validateGroups)
            if (constraintViolations.isNotEmpty()) {
                throw ConstraintViolationException(constraintViolations)
            }
        }
    }

    fun isEmptyCell(cellValue: Any?): Boolean {
        return cellValue == null || cellValue is CharSequence && !StringUtils.hasText(cellValue as CharSequence?)
    }

    //--------------------------------------------

    companion object {
        //--------------------------------------------
        @JvmStatic
        fun <T, P> index(title: String): ExcelField<T, P> {
            return ExcelField(title, indexColumn = true, imageColumn = false, formula = false)
        }

        @JvmStatic
        fun <T, P> formula(title: String, expression: String): ExcelField<T, P> {
            val excelField = ExcelField<T, P>(title, indexColumn = false, imageColumn = false, formula = true)
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
            return ExcelField(title, propertyGetter, indexColumn = false, imageColumn = false)
        }

        @JvmStatic
        fun <T, P> image(title: String, propertyGetter: ExcelConverter<T, P?>): ExcelField<T, P> {
            return ExcelField(title, propertyGetter, indexColumn = false, imageColumn = true)
        }

        /**
         * 只支持导出的初始化方法
         *
         * @param <P>            属性类型
         * @param <T>            实体类型
         * @param title          标题
         * @param propertyType   属性字段类型
         * @param propertyGetter 属性获取方法
         * @return Excel字段描述
        </T></P> */
        @JvmStatic
        fun <T, P> of(title: String, propertyType: Class<P>, propertyGetter: ExcelConverter<T, P?>): ExcelField<T, P> {
            return ExcelField(title, propertyType, propertyGetter, indexColumn = false, imageColumn = false)
        }

        /**
         * 支持导入及导出的初始化方法
         *
         * @param <P>            属性类型
         * @param <T>            实体类型
         * @param title          标题
         * @param propertyType   属性字段类型
         * @param propertyGetter 属性获取方法
         * @param propertySetter 属性设置方法
         * @return Excel字段描述
        </T></P> */
        @JvmStatic
        fun <T, P> of(title: String, propertyType: Class<P>, propertyGetter: ExcelConverter<T, P?>, propertySetter: ExcelCellSetter<T, P?>): ExcelField<T, P> {
            return ExcelField(title, propertyType, propertyGetter, indexColumn = false, imageColumn = false).setter(propertySetter)
        }

        //--------------------------------------------
        val primitiveWrapperTypeMap: MutableMap<Class<*>, Class<*>> = IdentityHashMap(8)

        init {
            primitiveWrapperTypeMap[Boolean::class.javaObjectType] = Boolean::class.javaPrimitiveType!!
            primitiveWrapperTypeMap[Byte::class.javaObjectType] = Byte::class.javaPrimitiveType!!
            primitiveWrapperTypeMap[Char::class.javaObjectType] = Char::class.javaPrimitiveType!!
            primitiveWrapperTypeMap[Double::class.javaObjectType] = Double::class.javaPrimitiveType!!
            primitiveWrapperTypeMap[Float::class.javaObjectType] = Float::class.javaPrimitiveType!!
            primitiveWrapperTypeMap[Int::class.javaObjectType] = Int::class.javaPrimitiveType!!
            primitiveWrapperTypeMap[Long::class.javaObjectType] = Long::class.javaPrimitiveType!!
            primitiveWrapperTypeMap[Short::class.javaObjectType] = Short::class.javaPrimitiveType!!
            primitiveWrapperTypeMap[Void::class.javaObjectType] = Void.TYPE
        }
    }
}
