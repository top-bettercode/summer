package cn.bestwu.generator.database.entity

import cn.bestwu.generator.GeneratorExtension
import cn.bestwu.generator.dom.java.JavaType
import cn.bestwu.generator.dom.java.JavaTypeResolver
import cn.bestwu.generator.dom.java.PrimitiveTypeWrapper
import cn.bestwu.generator.dsl.Generator

/**
 * 字段
 *
 * @author Peter Wu
 */
data class Column(
        val tableCat: String?,
        val tableSchem: String?,
        /**
         * 数据库字段名
         */
        val columnName: String,
        /**
         * 数据库字段类型
         */
        var typeName: String,
        /**
         * 字段类型
         */
        val dataType: Int?,
        /**
         * DECIMAL_DIGITS
         */
        var decimalDigits: Int,
        /**
         * COLUMN_SIZE
         */
        var columnSize: Int,
        /**
         * 注释说明
         */
        val remarks: String,
        /**
         * 是否可为空
         */
        var nullable: Boolean,
        /**
         * 默认值
         */
        var columnDef: String?,
        var extra: String = "",
        var unique: Boolean = false,
        var indexed: Boolean = false,
        var isPrimary: Boolean = false,
        var unsigned: Boolean = false,
        var isForeignKey: Boolean = false,
        var pktableName: String? = null,
        var pkcolumnName: String? = null,
        var autoIncrement: Boolean = false,
        var generatedColumn: Boolean = false
) {
    init {
        if ("null".equals(columnDef, true)) {
            columnDef = null
        }
    }

    val javaType: JavaType
        get() = JavaTypeResolver.calculateJavaType(this)!!
    val jdbcType: String
        get() = JavaTypeResolver.calculateJdbcTypeName(this)!!
    val javaName: String = GeneratorExtension.javaName(this.columnName)
    val typeDesc: String
        get() = "$typeName${if (containsSize) "($columnSize${if (decimalDigits > 0) ",$decimalDigits" else ""})" else ""}"
    val defaultDesc: String
        get() {
            val isString = typeName.startsWith("VARCHAR", true) || typeName.startsWith("TEXT", true) || typeName.startsWith("TINYTEXT", true) || typeName.startsWith("MEDIUMTEXT", true)
            return if (columnDef == null) "" else {
                val qt = if (isString) "'" else ""
                (" DEFAULT $qt$columnDef$qt")
            }
        }

    val containsSize: Boolean
        get() = columnSize > 0 && !arrayOf(java.lang.Object::class.java.name, "byte[]", java.util.Date::class.java.name, "java.time.OffsetTime", "java.time.OffsetDateTime", "java.time.LocalDate", "java.time.LocalTime", "java.time.LocalDateTime").contains(javaType.fullyQualifiedName) && !arrayOf("TINYTEXT", "MEDIUMTEXT", "TEXT", "CLOB", "NCLOB").contains(typeName.toUpperCase())

    val randomValue: Any
        get() = when {
            columnDef.isNullOrBlank() -> when (javaType) {
                JavaType("java.math.BigDecimal") -> java.math.BigDecimal("1.0")
                JavaType("java.sql.Timestamp") -> (System.currentTimeMillis())
                JavaType.dateInstance -> (System.currentTimeMillis())
                JavaType("java.sql.Date") -> (System.currentTimeMillis())
                JavaType("java.sql.Time") -> (System.currentTimeMillis())
                PrimitiveTypeWrapper.booleanInstance -> true
                PrimitiveTypeWrapper.doubleInstance -> 1.0
                PrimitiveTypeWrapper.longInstance -> 1L
                PrimitiveTypeWrapper.integerInstance -> 1
                JavaType.stringInstance -> remarks.replace("\"", "\\\"")
                else -> 1
            }
            columnDef == "CURRENT_TIMESTAMP" -> (System.currentTimeMillis())
            else -> columnDef!!
        }

    val randomValueToSet: String
        get() = if (initializationString.isNullOrBlank()) {
            when (javaType) {
                JavaType("java.math.BigDecimal") -> "new java.math.BigDecimal(\"1.0\")"
                JavaType("java.sql.Timestamp") -> "new java.sql.Timestamp(System.currentTimeMillis())"
                JavaType.dateInstance -> "new java.util.Date(System.currentTimeMillis())"
                JavaType("java.sql.Date") -> "new java.sql.Date(System.currentTimeMillis())"
                JavaType("java.sql.Time") -> "new java.sql.Time(System.currentTimeMillis())"
                PrimitiveTypeWrapper.booleanInstance -> "true"
                PrimitiveTypeWrapper.doubleInstance -> "1.0"
                PrimitiveTypeWrapper.longInstance -> "1L"
                PrimitiveTypeWrapper.integerInstance -> "1"
                PrimitiveTypeWrapper.shortInstance -> "new Short(\"1\")"
                PrimitiveTypeWrapper.byteInstance -> "new Byte(\"1\")"
                JavaType("byte[]") -> "new byte[0]"
                JavaType.stringInstance -> "\"${remarks.replace("\"", "\\\"")}\""
                else -> "1"
            }
        } else {
            initializationString!!
        }

    val testId: Any
        get() = when (javaType) {
            JavaType.stringInstance -> "\"1\""
            PrimitiveTypeWrapper.longInstance -> "1L"
            PrimitiveTypeWrapper.integerInstance -> 1
            else -> 1
        }

    val initializationString
        get() = if (!columnDef.isNullOrBlank()) {
            when {
                columnDef == "CURRENT_TIMESTAMP" -> "new Date()"
                javaType.shortName == "Boolean" -> Generator.toBoolean(columnDef).toString()
                javaType.shortName == "Long" -> "${columnDef}L"
                javaType.shortName == "Double" -> "${columnDef}D"
                javaType.shortName == "Float" -> "${columnDef}F"
                javaType.shortName == "BigDecimal" -> "new BigDecimal($columnDef)"
                javaType.shortName == "String" -> "\"$columnDef\""
                else -> columnDef
            }
        } else {
            columnDef
        }


    fun isSoftDelete(extension: GeneratorExtension): Boolean = columnName == extension.softDeleteColumnName

    fun jsonViewIgnored(extension: GeneratorExtension): Boolean = extension.jsonViewIgnoredFieldNames.contains(javaName)

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Column) return false

        if (columnName != other.columnName) return false
        if (!typeDesc.equals(other.typeDesc, true)) return false
        if (remarks != other.remarks) return false
        if (nullable != other.nullable) return false
        if (columnDef != other.columnDef) return false
        if (!extra.equals(other.extra, true)) return false
        if (isForeignKey != other.isForeignKey) return false
        if (pktableName != other.pktableName) return false
        if (pkcolumnName != other.pkcolumnName) return false
        if (generatedColumn != other.generatedColumn) return false
        if (unsigned != other.unsigned) return false
        if (autoIncrement != other.autoIncrement) return false

        return true
    }

    override fun hashCode(): Int {
        var result = columnName.hashCode()
        result = 31 * result + typeDesc.toUpperCase().hashCode()
        result = 31 * result + remarks.hashCode()
        result = 31 * result + nullable.hashCode()
        result = 31 * result + (columnDef?.hashCode() ?: 0)
        result = 31 * result + extra.toUpperCase().hashCode()
        result = 31 * result + isForeignKey.hashCode()
        result = 31 * result + (pktableName?.hashCode() ?: 0)
        result = 31 * result + (pkcolumnName?.hashCode() ?: 0)
        result = 31 * result + generatedColumn.hashCode()
        result = 31 * result + unsigned.hashCode()
        result = 31 * result + autoIncrement.hashCode()
        return result
    }
}