package top.bettercode.generator.database.entity

import top.bettercode.generator.GeneratorExtension
import top.bettercode.generator.dom.java.JavaType
import top.bettercode.generator.dom.java.JavaTypeResolver
import java.util.*

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
    val idgenerator: Boolean = false,
    val sequence: String = "",
    val sequenceStartWith: Int = 1,
    var generatedColumn: Boolean = false,
    var version: Boolean = false
) {
    init {
        if ("null".equals(columnDef, true)) {
            columnDef = null
        }
    }

    private val codeRemarks: String
        get() =
            remarks.replace('（', '(').replace('）', ')').replace('：', ':')
                .replace(Regex(" *: *"), ":").replace(Regex(" +"), " ")
                .replace('；', ';').replace(' ', ';').replace(Regex(";+"), ";")

    private val oldCodeRemarks: String
        get() = codeRemarks.replace('，', ',')
            .replace(Regex(",+"), ",")


    val prettyRemarks: String
        get() {
            return when {
                oldCodeRemarks.matches(Regex(".*\\((.*:.*[, ]?)+\\).*")) && !oldCodeRemarks.contains(
                    ";"
                ) -> {
                    oldCodeRemarks.replace(",", ";")
                }
                isCodeField -> {
                    codeRemarks
                }
                else -> {
                    remarks
                }
            }
        }

    val isCodeField: Boolean
        get() = codeRemarks.matches(Regex(".*\\((.*:.*[; ]?)+\\).*"))


    val javaType: JavaType
        get() = JavaTypeResolver.calculateJavaType(this)
    val jdbcType: String
        get() = JavaTypeResolver.calculateJdbcTypeName(this)
    val javaName: String = GeneratorExtension.javaName(this.columnName)
    val typeDesc: String
        get() = "$typeName${if (containsSize) "($columnSize${if (decimalDigits > 0) ",$decimalDigits" else ""})" else ""}"
    val defaultDesc: String
        get() {
            val isString = typeName.startsWith("VARCHAR", true) || typeName.startsWith(
                "TEXT",
                true
            ) || typeName.startsWith("TINYTEXT", true) || typeName.startsWith("MEDIUMTEXT", true)
            return if (columnDef == null) "" else {
                val qt = if (isString) "'" else ""
                (" DEFAULT $qt$columnDef$qt")
            }
        }

    val containsSize: Boolean
        get() = columnSize > 0 && !arrayOf(
            java.lang.Object::class.java.name,
            "byte[]",
            java.util.Date::class.java.name,
            "java.time.OffsetTime",
            "java.time.OffsetDateTime",
            "java.time.LocalDate",
            "java.time.LocalTime",
            "java.time.LocalDateTime"
        ).contains(javaType.fullyQualifiedName) && !arrayOf(
            "TINYTEXT",
            "MEDIUMTEXT",
            "TEXT",
            "CLOB",
            "NCLOB"
        ).contains(typeName.toUpperCase(Locale.getDefault()))

    fun isSoftDelete(extension: GeneratorExtension): Boolean =
        javaName == extension.softDeleteColumnName

    fun jsonViewIgnored(extension: GeneratorExtension): Boolean =
        extension.jsonViewIgnoredFieldNames.contains(javaName)

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Column) return false

        if (columnName != other.columnName) return false
        if (!typeDesc.equals(other.typeDesc, true)) return false
        if (remarks != other.remarks) return false
        if (nullable != other.nullable) return false
        if (!columnDefEquals(columnDef, other.columnDef)) return false
        if (!extra.equals(other.extra, true)) return false
        if (isForeignKey != other.isForeignKey) return false
        if (pktableName != other.pktableName) return false
        if (pkcolumnName != other.pkcolumnName) return false
        if (generatedColumn != other.generatedColumn) return false
        if (unsigned != other.unsigned) return false
        if (autoIncrement != other.autoIncrement) return false

        return true
    }

    private fun columnDefEquals(columnDef: String?, columnDef1: String?): Boolean {
        return if (columnDef == null)
            columnDef1 == null
        else {
            val decimal = columnDef.toBigDecimalOrNull()
            val decimal1 = columnDef1?.toBigDecimalOrNull()
            if (decimal != null && decimal1 != null) {
                val scale = decimal.scale()
                val scale1 = decimal1.scale()
                val s = scale.coerceAtLeast(scale1)
                decimal.setScale(s) == decimal1.setScale(s)
            } else
                columnDef == columnDef1
        }
    }

    override fun hashCode(): Int {
        var result = columnName.hashCode()
        result = 31 * result + typeDesc.toUpperCase(Locale.getDefault()).hashCode()
        result = 31 * result + remarks.hashCode()
        result = 31 * result + nullable.hashCode()
        result = 31 * result + (columnDef?.hashCode() ?: 0)
        result = 31 * result + extra.toUpperCase(Locale.getDefault()).hashCode()
        result = 31 * result + isForeignKey.hashCode()
        result = 31 * result + (pktableName?.hashCode() ?: 0)
        result = 31 * result + (pkcolumnName?.hashCode() ?: 0)
        result = 31 * result + generatedColumn.hashCode()
        result = 31 * result + unsigned.hashCode()
        result = 31 * result + autoIncrement.hashCode()
        return result
    }
}