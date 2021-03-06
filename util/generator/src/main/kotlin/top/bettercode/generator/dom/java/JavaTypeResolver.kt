package top.bettercode.generator.dom.java

import top.bettercode.generator.database.entity.Column
import top.bettercode.lang.property.PropertiesSource
import top.bettercode.lang.property.Settings
import java.sql.Types
import java.util.*

/**
 * @author Peter Wu
 */
object JavaTypeResolver {

    var forceBigDecimals: Boolean = false
    var forceIntegers: Boolean = true
    var useJSR310Types: Boolean = true

    private val typeMap: MutableMap<Int, JdbcTypeInformation> = HashMap()
    private val typeNameMap: MutableMap<String, Int> = HashMap()
    private val typeNames: PropertiesSource = Settings.jdbcTypeName

    init {

        typeMap[Types.ARRAY] = JdbcTypeInformation(
            "ARRAY",
            JavaType(java.lang.Object::class.java.name)
        )
        typeMap[Types.BIGINT] = JdbcTypeInformation(
            "BIGINT",
            JavaType(java.lang.Long::class.java.name)
        )
        typeMap[Types.BINARY] = JdbcTypeInformation(
            "BINARY",
            JavaType("byte[]")
        )
        typeMap[Types.BIT] = JdbcTypeInformation(
            "BIT",
            JavaType(java.lang.Boolean::class.java.name)
        )
        typeMap[Types.BLOB] = JdbcTypeInformation(
            "BLOB",
            JavaType("byte[]")
        )
        typeMap[Types.BOOLEAN] = JdbcTypeInformation(
            "BOOLEAN",
            JavaType(java.lang.Boolean::class.java.name)
        )
        typeMap[Types.CHAR] = JdbcTypeInformation(
            "CHAR",
            JavaType(java.lang.String::class.java.name)
        )
        typeMap[Types.CLOB] = JdbcTypeInformation(
            "CLOB",
            JavaType(java.lang.String::class.java.name)
        )
        typeMap[Types.DATALINK] = JdbcTypeInformation(
            "DATALINK",
            JavaType(java.lang.Object::class.java.name)
        )
        typeMap[Types.DATE] = JdbcTypeInformation(
            "DATE",
            JavaType(java.util.Date::class.java.name)
        )
        typeMap[Types.DECIMAL] = JdbcTypeInformation(
            "DECIMAL",
            JavaType(java.math.BigDecimal::class.java.name)
        )
        typeMap[Types.DISTINCT] = JdbcTypeInformation(
            "DISTINCT",
            JavaType(java.lang.Object::class.java.name)
        )
        typeMap[Types.DOUBLE] = JdbcTypeInformation(
            "DOUBLE",
            JavaType(java.lang.Double::class.java.name)
        )
        typeMap[Types.FLOAT] = JdbcTypeInformation(
            "FLOAT",
            JavaType(java.lang.Double::class.java.name)
        )
        typeMap[Types.INTEGER] = JdbcTypeInformation(
            "INTEGER",
            JavaType(java.lang.Integer::class.java.name)
        )
        typeMap[Types.JAVA_OBJECT] = JdbcTypeInformation(
            "JAVA_OBJECT",
            JavaType(java.lang.Object::class.java.name)
        )
        typeMap[Types.LONGNVARCHAR] = JdbcTypeInformation(
            "LONGNVARCHAR",
            JavaType(java.lang.String::class.java.name)
        )
        typeMap[Types.LONGVARBINARY] = JdbcTypeInformation(
            "LONGVARBINARY",
            JavaType("byte[]")
        )
        typeMap[Types.LONGVARCHAR] = JdbcTypeInformation(
            "LONGVARCHAR",
            JavaType(java.lang.String::class.java.name)
        )
        typeMap[Types.NCHAR] = JdbcTypeInformation(
            "NCHAR",
            JavaType(java.lang.String::class.java.name)
        )
        typeMap[Types.NCLOB] = JdbcTypeInformation(
            "NCLOB",
            JavaType(java.lang.String::class.java.name)
        )
        typeMap[Types.NVARCHAR] = JdbcTypeInformation(
            "NVARCHAR",
            JavaType(java.lang.String::class.java.name)
        )
        typeMap[Types.NULL] = JdbcTypeInformation(
            "NULL",
            JavaType(java.lang.Object::class.java.name)
        )
        typeMap[Types.NUMERIC] = JdbcTypeInformation(
            "NUMERIC",
            JavaType(java.math.BigDecimal::class.java.name)
        )
        typeMap[Types.OTHER] = JdbcTypeInformation(
            "OTHER",
            JavaType(java.lang.Object::class.java.name)
        )
        typeMap[Types.REAL] = JdbcTypeInformation(
            "REAL",
            JavaType(java.lang.Float::class.java.name)
        )
        typeMap[Types.REF] = JdbcTypeInformation(
            "REF",
            JavaType(java.lang.Object::class.java.name)
        )
        typeMap[Types.SMALLINT] = JdbcTypeInformation(
            "SMALLINT",
            JavaType(java.lang.Short::class.java.name)
        )
        typeMap[Types.STRUCT] = JdbcTypeInformation(
            "STRUCT",
            JavaType(java.lang.Object::class.java.name)
        )
        typeMap[Types.TIME] = JdbcTypeInformation(
            "TIME",
            JavaType(java.util.Date::class.java.name)
        )
        typeMap[Types.TIMESTAMP] = JdbcTypeInformation(
            "TIMESTAMP",
            JavaType(java.util.Date::class.java.name)
        )
        typeMap[Types.TINYINT] = JdbcTypeInformation(
            "TINYINT",
            JavaType(java.lang.Byte::class.java.name)
        )
        typeMap[Types.VARBINARY] = JdbcTypeInformation(
            "VARBINARY",
            JavaType("byte[]")
        )
        typeMap[Types.VARCHAR] = JdbcTypeInformation(
            "VARCHAR",
            JavaType(java.lang.String::class.java.name)
        )
        // JDK 1.8 types
        typeMap[Types.TIME_WITH_TIMEZONE] = JdbcTypeInformation(
            "TIME_WITH_TIMEZONE",
            JavaType("java.time.OffsetTime")
        )
        typeMap[Types.TIMESTAMP_WITH_TIMEZONE] = JdbcTypeInformation(
            "TIMESTAMP_WITH_TIMEZONE",
            JavaType("java.time.OffsetDateTime")
        )
        typeMap.forEach { (t, u) ->
            typeNameMap[u.jdbcTypeName] = t
        }
    }

    private fun calculateJdbcTypeName(typeName: String): String {
        val typeNameUpper = typeName.uppercase(Locale.getDefault())
        return typeNames.getOrDefault(typeNameUpper, typeNameUpper)
    }

    fun calculateDataType(jdbcTypeName: String): Int? {
        return typeNameMap[calculateJdbcTypeName(jdbcTypeName)]
    }

    fun calculateJavaType(column: Column): JavaType {
        var answer: JavaType
        val information = calculateJdbcTypeInformation(column)
        answer = information.javaType
        answer = overrideDefaultType(column, answer)

        return if (forceIntegers && (JavaType(java.lang.Short::class.java.name) == answer || JavaType(
                java.lang.Byte::class.java.name
            ) == answer)
        )
            JavaType(java.lang.Integer::class.java.name)
        else
            answer
    }

    fun calculateJdbcTypeName(column: Column): String {
        val jdbcTypeInformation = calculateJdbcTypeInformation(column)
        return jdbcTypeInformation.jdbcTypeName
    }

    private fun calculateJdbcTypeInformation(column: Column): JdbcTypeInformation {
        return typeMap[calculateDataType(column)]
            ?: throw IllegalStateException("????????????${column.typeName}?????????Java???????????????jdbcTypeName.properties???????????????")
    }

    private fun overrideDefaultType(column: Column, defaultType: JavaType): JavaType {
        var answer = defaultType

        when (calculateDataType(column)) {
            Types.BIT -> answer = calculateBitReplacement(column, defaultType)
            Types.DECIMAL, Types.NUMERIC -> answer =
                calculateBigDecimalReplacement(column, defaultType)
            Types.DATE -> answer = calculateDateType(defaultType)
            Types.TIME -> answer = calculateTimeType(defaultType)
            Types.TIMESTAMP -> answer = calculateTimestampType(defaultType)
            else -> {
            }
        }

        return answer
    }

    private fun calculateDataType(column: Column) =
        calculateDataType(column.typeName) ?: column.dataType
        ?: throw IllegalStateException("????????????${column.typeName}?????????dataType???????????????jdbcTypeName.properties???????????????")

    private fun calculateDateType(defaultType: JavaType): JavaType {
        return if (useJSR310Types) {
            JavaType("java.time.LocalDate")
        } else {
            defaultType
        }
    }

    private fun calculateTimeType(defaultType: JavaType): JavaType {
        return if (useJSR310Types) {
            JavaType("java.time.LocalTime")
        } else {
            defaultType
        }
    }

    private fun calculateTimestampType(defaultType: JavaType): JavaType {
        return if (useJSR310Types) {
            JavaType("java.time.LocalDateTime")
        } else {
            defaultType
        }
    }

    private fun calculateBitReplacement(column: Column, defaultType: JavaType): JavaType {
        return if (column.columnSize > 1) {
            JavaType("byte[]")
        } else {
            defaultType
        }
    }

    private fun calculateBigDecimalReplacement(column: Column, defaultType: JavaType): JavaType {
        return if (column.decimalDigits > 0 || column.columnSize > 20 || forceBigDecimals) {
            defaultType
        } else if (column.columnSize > 11 || column.columnSize == 0) {
            JavaType(java.lang.Long::class.java.name)
        } else if (column.columnSize > 4) {
            JavaType(java.lang.Integer::class.java.name)
        } else {
            JavaType(java.lang.Short::class.java.name)
        }
    }

    class JdbcTypeInformation(
        val jdbcTypeName: String,
        val javaType: JavaType
    )
}
