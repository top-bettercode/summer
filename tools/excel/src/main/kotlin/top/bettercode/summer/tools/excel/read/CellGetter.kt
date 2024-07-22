package top.bettercode.summer.tools.excel.read

import javassist.bytecode.SignatureAttribute
import org.dhatim.fastexcel.reader.CellType
import org.dhatim.fastexcel.reader.Row
import org.springframework.util.Assert
import org.springframework.util.ClassUtils
import org.springframework.util.ReflectionUtils
import top.bettercode.summer.tools.excel.Converter
import top.bettercode.summer.tools.excel.ExcelException
import top.bettercode.summer.tools.lang.capitalized
import top.bettercode.summer.tools.lang.decapitalized
import top.bettercode.summer.tools.lang.util.BooleanUtil.toBoolean
import top.bettercode.summer.tools.lang.util.TimeUtil
import top.bettercode.summer.tools.lang.util.TimeUtil.Companion.DEFAULT_DATE_TIME_FORMAT_PATTERN
import top.bettercode.summer.web.resolver.UnitConverter
import top.bettercode.summer.web.support.code.CodeServiceHolder
import java.lang.invoke.SerializedLambda
import java.math.BigDecimal
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.*
import java.util.function.Consumer
import javax.validation.ConstraintViolationException
import javax.validation.Validator
import kotlin.collections.MutableMap
import kotlin.collections.filter
import kotlin.collections.isNotEmpty
import kotlin.collections.joinToString
import kotlin.collections.map
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
class CellGetter<E, P> {
    /**
     * 字段标题
     */
    val title: String

    /**
     * 实体类型
     */
    val entityType: Class<E>?

    /**
     * 属性字段名称
     */
    private val propertyName: String?

    /**
     * 属性字段类型
     */
    private val propertyType: Class<P>

    /**
     * 设置实体属性
     */
    private val propertySetter: PropertySetter<E, P?>

    /**
     * 单元格值转属性字段值
     */
    private var converter: ((Any) -> P?)? = null

    /**
     * 默认单元格值转属性字段值
     */
    var dateFormat: String = DEFAULT_DATE_TIME_FORMAT_PATTERN

    /**
     * 是否时间日期字段
     */
    var isDate: Boolean = false

    /**
     * 属性字段值验证
     */
    var validator: Consumer<E>? = null

    /**
     * 默认值
     */
    private var defaultValue: P? = null

    //--------------------------------------------
    constructor(title: String, propertyType: Class<P>, propertySetter: PropertySetter<E, P?>) {
        this.title = title
        this.propertyType = propertyType
        this.entityType = null
        this.propertyName = null
        this.propertySetter = propertySetter
    }

    constructor(title: String, propertyGetter: Converter<E, P?>) {
        var propertyType: Class<P>? = null
        var propertyName: String? = null
        var entityType: Class<E>? = null
        var propertySetter: PropertySetter<E, P?>? = null
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
                        @Suppress("UNCHECKED_CAST")
                        entityType = (get.owner as KClass<*>).java as Class<E>
                        propertyName = get.name

                        val writeMethod = try {
                            entityType.getMethod("set" + propertyName.capitalized(), propertyType)
                        } catch (e: NoSuchMethodException) {
                            if (ClassUtils.isPrimitiveWrapper(propertyType)) {
                                @Suppress("UNCHECKED_CAST")
                                propertyType = primitiveWrapperTypeMap[propertyType] as Class<P>
                                entityType.getMethod(
                                    "set" + propertyName.capitalized(),
                                    propertyType
                                )
                            } else {
                                throw e
                            }
                        }
                        propertySetter = PropertySetter { entity, property ->
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
                @Suppress("UNCHECKED_CAST")
                propertyType =
                    ClassUtils.forName(methodSignature.returnType.jvmTypeName(), null) as Class<P>
                propertyName = resolvePropertyName(implMethodName)
                @Suppress("UNCHECKED_CAST")
                entityType = ClassUtils.forName(
                    methodSignature.parameterTypes[0].jvmTypeName(),
                    null
                ) as Class<E>
                //$lamda-0
                if (!propertyName.contains("lambda\$new$") && !propertyName.contains("\$lambda")) {
                    val writeMethod = try {
                        entityType.getMethod("set" + propertyName.capitalized(), propertyType)
                    } catch (e: NoSuchMethodException) {
                        if (ClassUtils.isPrimitiveWrapper(propertyType)) {
                            @Suppress("UNCHECKED_CAST")
                            propertyType = primitiveWrapperTypeMap[propertyType] as Class<P>
                            entityType.getMethod(
                                "set" + propertyName.capitalized(),
                                propertyType
                            )
                        } else {
                            throw e
                        }
                    }
                    propertySetter = PropertySetter { entity, property ->
                        ReflectionUtils.invokeMethod(writeMethod, entity, property)
                    }
                } else {
                    propertyName = null
                }
            }
        } catch (e: Exception) {
            throw ExcelException("属性解析错误", e)
        }
        this.title = title
        this.propertyName = propertyName
        this.propertyType = propertyType
        this.entityType = entityType
        this.propertySetter = propertySetter ?: throw ExcelException("属性set方法解析错误")
        isDate =
            propertyType == LocalDate::class.java || propertyType == LocalDateTime::class.java || propertyType == Date::class.java
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
    @JvmOverloads
    fun scale(scale: Int = 2): CellGetter<E, P> {
        return unit(1, scale)
    }

    @JvmOverloads
    fun yuan(scale: Int = 2): CellGetter<E, P> {
        return unit(100, scale)
    }

    @JvmOverloads
    fun unit(value: Int, scale: Int = log10(value.toDouble()).toInt()): CellGetter<E, P> {
        return converter { cell: Any ->
            UnitConverter.smaller(
                number = BigDecimal(cell.toString()),
                propertyType,
                value = value,
                scale = scale
            )
        }
    }

    fun code(): CellGetter<E, P> {
        Assert.hasText(propertyName, "属性名称未设置")
        return code(propertyName!!)
    }

    fun codeServiceRef(codeServiceRef: String): CellGetter<E, P> {
        Assert.hasText(propertyName, "属性名称未设置")
        return code(codeServiceRef, propertyName!!)
    }

    fun code(codeType: String): CellGetter<E, P> {
        return code("", codeType)
    }

    fun code(codeServiceRef: String, codeType: String): CellGetter<E, P> {
        return converter { cellValue: Any ->
            val separator = ","
            val value = cellValue.toString()
            val dicCodes = CodeServiceHolder.get(codeServiceRef, codeType)
            @Suppress("UNCHECKED_CAST")
            return@converter if (value.contains(separator)) {
                val split = value.split(separator).filter { it.isNotBlank() }.map { it.trim() }

                split.joinToString(separator) { s: String ->
                    dicCodes?.getCode(s)?.toString()
                        ?: throw IllegalArgumentException("无\"$s\"对应的类型")
                }
            } else {
                dicCodes?.getCode(value)
                    ?: throw IllegalArgumentException("无\"$cellValue\"对应的类型")
            } as P?
        }
    }

    /**
     * @param format 日期格式化
     * @return this
     */
    @JvmOverloads
    fun date(format: String = DEFAULT_DATE_TIME_FORMAT_PATTERN): CellGetter<E, P> {
        this.dateFormat = format
        this.isDate = true
        return this
    }

    /**
     * 设置默认值
     */
    fun defaultValue(defaultValue: P): CellGetter<E, P> {
        this.defaultValue = defaultValue
        return this
    }

    //--------------------------------------------
    /**
     * 单元格值转属性字段值
     */
    fun converter(converter: (Any) -> P?): CellGetter<E, P> {
        this.converter = converter
        return this
    }

    fun validator(validator: Consumer<E>): CellGetter<E, P> {
        this.validator = validator
        return this
    }

    //--------------------------------------------

    /**
     * @param cellValue      单元格值
     * @param validator      参数验证
     * @param validateGroups 参数验证组
     */
    fun E.setProperty(
        cellValue: Any?,
        validator: Validator,
        validateGroups: Array<Class<*>>
    ) {
        val property: P? = cellValue?.toProperty(
            propertyType = propertyType,
            dateFormat = dateFormat,
            isDate = isDate,
            converter = converter
        ) ?: defaultValue
        propertySetter.let { it[this] = property }
        if (propertyName != null) {
            val constraintViolations =
                validator.validateProperty<Any>(this, propertyName, *validateGroups)
            if (constraintViolations.isNotEmpty()) {
                throw ConstraintViolationException(constraintViolations)
            }
        }
    }

    //--------------------------------------------
    companion object {

        /**
         * @param <P>            属性类型
         * @param <T>            实体类型
         * @param title          标题
         * @param propertyGetter 属性获取方法
         * @return Excel字段描述
        </T></P> */
        @JvmStatic
        fun <T, P> of(
            title: String, propertyGetter: Converter<T, P?>
        ): CellGetter<T, P> {
            return CellGetter(title, propertyGetter)
        }

        @JvmStatic
        fun <T, P> of(
            title: String,
            propertyType: Class<P>,
            setter: PropertySetter<T, P?>
        ): CellGetter<T, P> {
            return CellGetter(title, propertyType, setter)
        }

        //--------------------------------------------

        @JvmStatic
        @JvmOverloads
        fun Row.string(
            column: Int, converter: ((Any) -> String?)? = null,
        ): String? {
            return property(
                column = column,
                propertyType = String::class.java,
                converter = converter
            )
        }

        @JvmStatic
        @JvmOverloads
        fun Row.int(
            column: Int,
            converter: ((Any) -> Int?)? = null,
        ): Int? {
            return property(
                column = column,
                propertyType = Int::class.java,
                converter = converter
            )
        }

        @JvmStatic
        @JvmOverloads
        fun Row.long(
            column: Int,
            converter: ((Any) -> Long?)? = null,
        ): Long? {
            return property(
                column = column,
                propertyType = Long::class.java,
                converter = converter
            )
        }

        @JvmStatic
        @JvmOverloads
        fun Row.millis(
            column: Int,
            converter: ((Any) -> Long?)? = null,
        ): Long? {
            return property(
                column = column,
                propertyType = Long::class.java,
                converter = converter,
                isDate = true
            )
        }


        @JvmStatic
        @JvmOverloads
        fun Row.bigDecimal(
            column: Int,
            converter: ((Any) -> BigDecimal?)? = null,
        ): BigDecimal? {
            return property(
                column = column,
                propertyType = BigDecimal::class.java,
                converter = converter
            )
        }

        @JvmStatic
        @JvmOverloads
        fun Row.date(
            column: Int,
            dateFormat: String = DEFAULT_DATE_TIME_FORMAT_PATTERN,
            converter: ((Any) -> Date?)? = null,
        ): Date? {
            return property(
                column = column,
                propertyType = Date::class.java,
                dateFormat = dateFormat,
                isDate = true,
                converter = converter
            )
        }

        @JvmStatic
        @JvmOverloads
        fun Row.localDateTime(
            column: Int,
            dateFormat: String = DEFAULT_DATE_TIME_FORMAT_PATTERN,
            converter: ((Any) -> LocalDateTime?)? = null,
        ): LocalDateTime? {
            return property(
                column = column,
                propertyType = LocalDateTime::class.java,
                dateFormat = dateFormat,
                isDate = true,
                converter = converter
            )
        }

        @JvmStatic
        @JvmOverloads
        fun Row.localDate(
            column: Int,
            dateFormat: String = DEFAULT_DATE_TIME_FORMAT_PATTERN,
            converter: ((Any) -> LocalDate?)? = null,
        ): LocalDate? {
            return property(
                column = column,
                propertyType = LocalDate::class.java,
                dateFormat = dateFormat,
                isDate = true,
                converter = converter
            )
        }

        @JvmStatic
        @JvmOverloads
        fun Row.boolean(
            column: Int,
            converter: ((Any) -> Boolean?)? = null,
        ): Boolean? {
            return property(
                column = column,
                propertyType = Boolean::class.java,
                converter = converter
            )
        }

        @JvmStatic
        @JvmOverloads
        fun Row.double(
            column: Int,
            converter: ((Any) -> Double?)? = null,
        ): Double? {
            return property(
                column = column,
                propertyType = Double::class.java,
                converter = converter
            )
        }

        @JvmStatic
        @JvmOverloads
        fun Row.float(
            column: Int,
            converter: ((Any) -> Float?)? = null,
        ): Float? {
            return property(
                column = column,
                propertyType = Float::class.java,
                converter = converter
            )
        }

        @JvmStatic
        @JvmOverloads
        fun <P> Row.property(
            column: Int,
            propertyType: Class<P>? = null,
            dateFormat: String = DEFAULT_DATE_TIME_FORMAT_PATTERN,
            isDate: Boolean = false,
            converter: ((Any) -> P?)? = null,
        ): P? {
            return value(column = column, isDate = isDate)?.toProperty(
                propertyType = propertyType,
                dateFormat = dateFormat,
                isDate = isDate,
                converter = converter,
            )
        }


        @JvmStatic
        @JvmOverloads
        fun Row.value(column: Int, isDate: Boolean = false): Any? {
            val value = getOptionalCell(column).map {
                when (it.type) {
                    CellType.STRING -> it.asString()
                    CellType.NUMBER -> if (isDate) {
                        it.asDate()
                    } else {
                        it.asNumber()
                    }

                    CellType.BOOLEAN -> it.asBoolean()
                    else -> it.value
                }
            }.orElse(null)
            return value
        }


        @JvmStatic
        fun Any?.isNullOrBlank(): Boolean {
            return this == null || this is CharSequence && this.isBlank()
        }


        private fun <P> Any?.toProperty(
            propertyType: Class<P>? = null,
            dateFormat: String = DEFAULT_DATE_TIME_FORMAT_PATTERN,
            isDate: Boolean = false,
            converter: ((Any) -> P?)? = null,
        ): P? {
            return if (this.isNullOrBlank()) {
                null
            } else {
                if (converter == null) {
                    Assert.notNull(propertyType, "propertyType 不能为空")
                    convert(
                        propertyType = propertyType!!,
                        value = this!!,
                        dateFormat = dateFormat,
                        isDate = isDate
                    )
                } else {
                    converter(this!!)
                }
            }
        }

        private fun <P> convert(
            value: Any,
            propertyType: Class<P>,
            dateFormat: String,
            isDate: Boolean,
        ): P? {
            @Suppress("UNCHECKED_CAST")
            return when (propertyType) {
                String::class.java -> {
                    value.toString()
                }


                Int::class.javaObjectType, Int::class.java -> {
                    if (value is String) {
                        BigDecimal(value).toInt()
                    } else (value as BigDecimal).toInt()
                }

                Long::class.javaObjectType, Long::class.java -> {
                    when {
                        isDate -> {
                            when (value) {
                                is LocalDateTime -> {
                                    TimeUtil.of(value).toMillis()
                                }

                                is String -> {
                                    TimeUtil.parse(value, dateFormat)
                                        .toMillis()
                                }

                                else -> {
                                    throw ExcelException("转换为毫秒数失败")
                                }
                            }
                        }

                        else -> {
                            when (value) {
                                is String -> {
                                    BigDecimal(value).toLong()
                                }

                                else -> (value as BigDecimal).toLong()
                            }
                        }
                    }
                }

                BigDecimal::class.java -> {
                    when (value) {
                        is String -> {
                            BigDecimal(value)
                        }

                        else -> value
                    }
                }

                Date::class.java -> {
                    when (value) {
                        is LocalDateTime -> {
                            TimeUtil.of(value).toDate()
                        }

                        is String -> {
                            TimeUtil.parse(value, dateFormat).toDate()
                        }

                        else -> {
                            throw ExcelException("转换为时间失败")
                        }
                    }
                }

                LocalDateTime::class.java -> {
                    when (value) {
                        is LocalDateTime -> {
                            value
                        }

                        is String -> {
                            TimeUtil.parse(value, dateFormat)
                                .toLocalDateTime()
                        }

                        else -> {
                            throw ExcelException("转换为时间失败")
                        }
                    }
                }

                LocalDate::class.java -> {
                    when (value) {
                        is LocalDateTime -> {
                            value.toLocalDate()
                        }

                        is String -> {
                            TimeUtil.parse(value, dateFormat)
                                .toLocalDate()
                        }

                        else -> {
                            throw ExcelException("转换为时间失败")
                        }
                    }
                }

                Boolean::class.javaObjectType, Boolean::class.java -> {
                    value as? Boolean ?: toBoolean(value.toString())
                }

                Double::class.javaObjectType, Double::class.java -> {
                    when (value) {
                        is String -> {
                            BigDecimal(value).toDouble()
                        }

                        else -> (value as BigDecimal).toDouble()
                    }
                }

                Float::class.javaObjectType, Float::class.java -> {
                    when (value) {
                        is String -> {
                            BigDecimal(value).toFloat()
                        }

                        else -> (value as BigDecimal).toFloat()
                    }
                }

                else -> {
                    throw IllegalArgumentException("不支持的数据类型:" + propertyType.name)
                }
            } as P?
        }

        //--------------------------------------------
        @JvmStatic
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
