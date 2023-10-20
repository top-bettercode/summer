package top.bettercode.summer.tools.generator.database.entity

import top.bettercode.summer.tools.generator.GeneratorExtension
import top.bettercode.summer.tools.generator.dom.java.JavaType
import top.bettercode.summer.tools.generator.dom.java.JavaTypeResolver
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
        var remarks: String,
        /**
         * 是否可为空
         */
        var nullable: Boolean,
        /**
         * 默认值
         */
        var columnDef: String?,
        var unsigned: Boolean = false,
        var autoIncrement: Boolean = false,
        var generatedColumn: Boolean = false,
        var isForeignKey: Boolean = false,
        var pktableName: String? = null,
        var pkcolumnName: String? = null,
        var extra: String = "",
        var unique: Boolean = false,
        var indexed: Boolean = false,
        var isPrimary: Boolean = false,

        var idgenerator: String = "",
        var idgeneratorParam: String = "",
        var sequence: String = "",
        var sequenceStartWith: Long = 1,
        var version: Boolean = false,
        var logicalDelete: Boolean = false,
        var createdDate: Boolean = false,
        var createdBy: Boolean = false,
        var lastModifiedDate: Boolean = false,
        var lastModifiedBy: Boolean = false,
        var asBoolean: Boolean = false
) {
    init {
        if ("null".equals(columnDef, true)) {
            columnDef = null
        }
    }

    private val codeRemarks: String by lazy {
        remarks.replace('（', '(').replace('）', ')').replace('：', ':')
                .replace(Regex(" *: *"), ":").replace(Regex(" +"), " ")
                .replace('；', ';').replace(' ', ';').replace(Regex(";+"), ";")
    }

    private val oldCodeRemarks: String by lazy {
        codeRemarks.replace('，', ',')
                .replace(Regex(",+"), ",")
    }

    val prettyRemarks: String by lazy {
        when {
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

    val isCodeField: Boolean by lazy {
        javaType != JavaType.booleanWrapper && codeRemarks.matches(Regex(".*\\((.*:.*[; ]?)+\\).*"))
    }

    val originJavaType: JavaType
            by lazy { JavaTypeResolver.calculateJavaType(this) }


    val javaName: String = GeneratorExtension.javaName(this.columnName)

    val defaultDesc: String by lazy {
        val isString = typeName.startsWith("VARCHAR", true) || typeName.startsWith(
                "TEXT",
                true
        ) || typeName.startsWith("TINYTEXT", true) || typeName.startsWith("MEDIUMTEXT", true)
        if (columnDef == null) "" else {
            val qt = if (isString) "'" else ""
            (" DEFAULT $qt$columnDef$qt")
        }
    }

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
        return autoIncrement == other.autoIncrement
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
        result = 31 * result + typeDesc.uppercase(Locale.getDefault()).hashCode()
        result = 31 * result + remarks.hashCode()
        result = 31 * result + nullable.hashCode()
        result = 31 * result + (columnDef?.hashCode() ?: 0)
        result = 31 * result + extra.uppercase(Locale.getDefault()).hashCode()
        result = 31 * result + isForeignKey.hashCode()
        result = 31 * result + (pktableName?.hashCode() ?: 0)
        result = 31 * result + (pkcolumnName?.hashCode() ?: 0)
        result = 31 * result + generatedColumn.hashCode()
        result = 31 * result + unsigned.hashCode()
        result = 31 * result + autoIncrement.hashCode()
        return result
    }

    override fun toString(): String {
        return "Column(tableCat=$tableCat, tableSchem=$tableSchem, columnName='$columnName', typeName='$typeName', dataType=$dataType, decimalDigits=$decimalDigits, columnSize=$columnSize, remarks='$remarks', nullable=$nullable, columnDef=$columnDef, extra='$extra', unique=$unique, indexed=$indexed, isPrimary=$isPrimary, unsigned=$unsigned, isForeignKey=$isForeignKey, pktableName=$pktableName, pkcolumnName=$pkcolumnName, autoIncrement=$autoIncrement, idgenerator='$idgenerator', sequence='$sequence', sequenceStartWith=$sequenceStartWith, generatedColumn=$generatedColumn, version=$version, logicalDelete=$logicalDelete, asBoolean=$asBoolean, javaName='$javaName')"
    }


    //--------------------------------------------

    val javaType: JavaType
            by lazy {
                if (this.asBoolean) {
                    JavaType("java.lang.Boolean")
                } else
                    this.originJavaType
            }

    val numericBooleanType: Boolean
            by lazy {
                this.asBoolean && (
                        JavaType(java.lang.Integer::class.java.name) == this.originJavaType
                                || JavaType(java.lang.Short::class.java.name) == this.originJavaType
                                || JavaType(java.lang.Byte::class.java.name) == this.originJavaType)
            }

    val jsonViewIgnored: Boolean by lazy {
        this.logicalDelete || this.version
    }

    val testIgnored: Boolean by lazy {
        this.jsonViewIgnored || javaName == "createdDate" || javaName == "lastModifiedDate"
    }

    val jdbcType: String
            by lazy { JavaTypeResolver.calculateJdbcTypeName(this) }

    val typeDesc: String
            by lazy { "$typeName${if (containsSize) "($columnSize${if (decimalDigits > 0) ",$decimalDigits" else ""})" else ""}" }

    val containsSize: Boolean by lazy {
        columnSize > 0 && !arrayOf(
                "java.lang.Object",
                "byte[]",
                "java.util.Date",
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
        ).contains(typeName.uppercase(Locale.getDefault()))
    }

    val isAutoGenerated: Boolean by lazy {
        autoIncrement || idgenerator.isNotBlank() || sequence.isNotBlank()
    }


}