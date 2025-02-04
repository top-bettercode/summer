package top.bettercode.summer.tools.lang.util

import top.bettercode.summer.tools.lang.property.PropertiesSource
import top.bettercode.summer.tools.lang.property.Settings
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
    private val javaTypeMap: MutableMap<JavaType, String> = HashMap()
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
        javaTypeMap[JavaType("java.time.OffsetDateTime")] = "TIMESTAMP_WITH_TIMEZONE"
        javaTypeMap[JavaType("java.time.OffsetTime")] = "TIME_WITH_TIMEZONE"
        javaTypeMap[JavaType("byte[]")] = "VARBINARY"
        javaTypeMap[JavaType(java.util.Date::class.java.name)] = "DATE"
        javaTypeMap[JavaType(java.lang.Boolean::class.java.name)] = "BOOLEAN"
        javaTypeMap[JavaType(java.lang.Character::class.java.name)] = "CHAR"
        javaTypeMap[JavaType(java.lang.String::class.java.name)] = "VARCHAR"
        javaTypeMap[JavaType(java.lang.Byte::class.java.name)] = "TINYINT"
        javaTypeMap[JavaType(java.lang.Short::class.java.name)] = "SMALLINT"
        javaTypeMap[JavaType(java.lang.Float::class.java.name)] = "FLOAT"
        javaTypeMap[JavaType(java.lang.Double::class.java.name)] = "DOUBLE"
        javaTypeMap[JavaType(java.lang.Integer::class.java.name)] = "INTEGER"
        javaTypeMap[JavaType(java.lang.Long::class.java.name)] = "BIGINT"
        javaTypeMap[JavaType(java.math.BigDecimal::class.java.name)] = "DECIMAL"
    }

    fun type(dataType: Int): JdbcTypeInformation? {
        return typeMap[dataType]
    }

    fun type(jdbcTypeName: String): JdbcTypeInformation? {
        return typeMap[calculateDataType(jdbcTypeName)]
    }

    fun calculateJdbcTypeName(javaType: JavaType): String {
        return javaTypeMap.getOrDefault(javaType, "VARCHAR")
    }

    private fun calculateJdbcTypeName(typeName: String): String {
        val typeNameUpper = typeName.uppercase(Locale.getDefault())
        return typeNames.getOrDefault(typeNameUpper, typeNameUpper)
    }

    fun calculateDataType(jdbcTypeName: String): Int? {
        return typeNameMap[calculateJdbcTypeName(jdbcTypeName)]
    }

    class JdbcTypeInformation(
        val jdbcTypeName: String,
        val javaType: JavaType
    )
}
