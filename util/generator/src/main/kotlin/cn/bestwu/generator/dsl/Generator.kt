package cn.bestwu.generator.dsl

import cn.bestwu.generator.GeneratorException
import cn.bestwu.generator.GeneratorExtension
import cn.bestwu.generator.database.entity.Column
import cn.bestwu.generator.database.entity.Indexed
import cn.bestwu.generator.database.entity.Table
import cn.bestwu.generator.dom.java.JavaType
import cn.bestwu.generator.dom.java.PrimitiveTypeWrapper
import java.io.File
import java.io.PrintWriter

/**
 * 模板基类
 */
abstract class Generator {
    companion object {
        const val DEFAULT_NAME = "generated"

        fun toBoolean(obj: Any?): Boolean {
            when (obj) {
                is Boolean -> return obj
                is String -> {
                    if (obj.equals("true", true)) {
                        return true
                    }
                    when (obj.length) {
                        1 -> {
                            val ch0 = obj[0]
                            if (ch0 == 'y' || ch0 == 'Y' ||
                                ch0 == 't' || ch0 == 'T' || ch0 == '1'
                            ) {
                                return true
                            }
                            if (ch0 == 'n' || ch0 == 'N' ||
                                ch0 == 'f' || ch0 == 'F' || ch0 == '0'
                            ) {
                                return false
                            }
                        }
                        2 -> {
                            val ch0 = obj[0]
                            val ch1 = obj[1]
                            if ((ch0 == 'o' || ch0 == 'O') && (ch1 == 'n' || ch1 == 'N')) {
                                return true
                            }
                            if ((ch0 == 'n' || ch0 == 'N') && (ch1 == 'o' || ch1 == 'O')) {
                                return false
                            }
                        }
                        3 -> {
                            val ch0 = obj[0]
                            val ch1 = obj[1]
                            val ch2 = obj[2]
                            if ((ch0 == 'y' || ch0 == 'Y') &&
                                (ch1 == 'e' || ch1 == 'E') &&
                                (ch2 == 's' || ch2 == 'S')
                            ) {
                                return true
                            }
                            if ((ch0 == 'o' || ch0 == 'O') &&
                                (ch1 == 'f' || ch1 == 'F') &&
                                (ch2 == 'f' || ch2 == 'F')
                            ) {
                                return false
                            }
                        }
                        4 -> {
                            val ch0 = obj[0]
                            val ch1 = obj[1]
                            val ch2 = obj[2]
                            val ch3 = obj[3]
                            if ((ch0 == 't' || ch0 == 'T') &&
                                (ch1 == 'r' || ch1 == 'R') &&
                                (ch2 == 'u' || ch2 == 'U') &&
                                (ch3 == 'e' || ch3 == 'E')
                            ) {
                                return true
                            }
                        }
                        5 -> {
                            val ch0 = obj[0]
                            val ch1 = obj[1]
                            val ch2 = obj[2]
                            val ch3 = obj[3]
                            val ch4 = obj[4]
                            if ((ch0 == 'f' || ch0 == 'F') &&
                                (ch1 == 'a' || ch1 == 'A') &&
                                (ch2 == 'l' || ch2 == 'L') &&
                                (ch3 == 's' || ch3 == 'S') &&
                                (ch4 == 'e' || ch4 == 'E')
                            ) {
                                return false
                            }
                        }
                        else -> {
                        }
                    }
                }
                is Number -> return obj.toInt() > 0
            }

            return false
        }

    }

    fun Column.dicCodes(extension: GeneratorExtension): DicCodes? {
        return if (isCodeField) {
            val codeType = if (columnName.contains("_") || extension.commonCodeTypes.any {
                    it.equals(
                        columnName,
                        true
                    )
                }) javaName else (className + javaName.capitalize()).decapitalize()
            val codeRemarks = codeRemarks
            val codeTypeName = codeRemarks.substringBefore('(')

            val dicCodes = DicCodes(
                codeType,
                codeTypeName,
                JavaType.stringInstance != javaType
            )
            codeRemarks.substringAfter('(').substringBeforeLast(')').replace(" ", ",")
                .replace(Regex(",+"), ",").split(",").filter { it.isNotBlank() }
                .forEach { item: String ->
                    val code = item.substringBefore(":").trim()
                    val name = item.substringAfter(":").trim()
                    dicCodes.codes[code] = name
                }

            return dicCodes
        } else {
            null
        }
    }

    val Column.randomValue: Any
        get() = when {
            columnDef.isNullOrBlank() || "CURRENT_TIMESTAMP".equals(columnDef, true) ->
                when {
                    isCodeField -> dicCodes(extension)!!.codes.keys.first()
                    else ->
                        when (javaType) {
                            JavaType("java.math.BigDecimal") -> java.math.BigDecimal("1.0")
                            JavaType("java.sql.Timestamp") -> (System.currentTimeMillis())
                            JavaType.dateInstance -> (System.currentTimeMillis())
                            JavaType("java.sql.Date") -> (System.currentTimeMillis())
                            JavaType("java.sql.Time") -> (System.currentTimeMillis())
                            JavaType("java.time.LocalDate") -> (System.currentTimeMillis())
                            JavaType("java.time.LocalDateTime") -> (System.currentTimeMillis())
                            PrimitiveTypeWrapper.booleanInstance -> true
                            PrimitiveTypeWrapper.doubleInstance -> 1.0
                            PrimitiveTypeWrapper.longInstance -> 1L
                            PrimitiveTypeWrapper.integerInstance -> 1
                            JavaType.stringInstance -> remarks.replace("\"", "\\\"")
                            else -> 1
                        }
                }
            else -> columnDef!!
        }

    val Column.randomValueToSet: String
        get() =
            when {
                initializationString.isNullOrBlank() || "CURRENT_TIMESTAMP".equals(
                    columnDef,
                    true
                ) -> {
                    when {
                        isCodeField -> {
                            val value = dicCodes(extension)!!.codes.keys.first()
                            if (JavaType.stringInstance == javaType) "\"$value\"" else "$value"
                        }
                        else -> when (javaType) {
                            JavaType("java.math.BigDecimal") -> "new java.math.BigDecimal(\"1.0\")"
                            JavaType("java.sql.Timestamp") -> "new java.sql.Timestamp(System.currentTimeMillis())"
                            JavaType.dateInstance -> "new java.util.Date(System.currentTimeMillis())"
                            JavaType("java.sql.Date") -> "new java.sql.Date(System.currentTimeMillis())"
                            JavaType("java.sql.Time") -> "new java.sql.Time(System.currentTimeMillis())"
                            JavaType("java.time.LocalDate") -> "java.time.LocalDate.now()"
                            JavaType("java.time.LocalDateTime") -> "java.time.LocalDateTime.now()"
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
                    }
                }
                else -> initializationString!!
            }

    val Column.testId: Any
        get() = when (javaType) {
            JavaType.stringInstance -> "\"1\""
            PrimitiveTypeWrapper.longInstance -> "1L"
            PrimitiveTypeWrapper.integerInstance -> 1
            else -> 1
        }

    private val Column.initializationString
        get() = if (!columnDef.isNullOrBlank()) {
            when (javaType.shortName) {
                "Boolean" -> toBoolean(columnDef).toString()
                "Long" -> "${columnDef}L"
                "Double" -> "${columnDef}D"
                "Float" -> "${columnDef}F"
                "BigDecimal" -> "new BigDecimal($columnDef)"
                "String" -> "\"$columnDef\""
                else -> columnDef
            }
        } else {
            columnDef
        }


    fun Column.setValue(value: String): String {
        return when (javaType.shortName) {
            "Boolean" -> "Boolean.valueOf($value)"
            "Integer" -> "Integer.valueOf($value)"
            "Long" -> "Long.valueOf($value)"
            "Double" -> "Double.valueOf($value)"
            "Float" -> "Float.valueOf($value)"
            "BigDecimal" -> "new BigDecimal($value)"
            else -> value
        }
    }

    protected val Column.defaultRemarks: String
        get() = defaultDesc.replace("DEFAULT ", "默认值：")
    protected lateinit var table: Table
    protected lateinit var extension: GeneratorExtension
    protected val isOracleDatasource
        get() = extension.datasource.isOracle

    open var cover: Boolean = false
    protected open val test: Boolean = false
    protected open val resources: Boolean = false

    protected open val dir: String
        get() {
            val dir = if (test) extension.dir.replace("src/main/", "src/test/") else extension.dir
            return if (resources) dir.replace("java", "resources") else dir
        }

    protected val basePath: File
        get() = extension.basePath

    /**
     * 文件名称
     */
    protected open val name: String = DEFAULT_NAME

    protected open val destFile: File
        get() {
            return File(
                File(basePath, dir),
                if (this is JavaGenerator && !resources) "${
                    name.replace(
                        ".",
                        File.separator
                    )
                }.java" else name
            )
        }

    open var module: String = ""
        get() = if (extension.module.isNotBlank()) extension.module else field

    open val moduleName: String
        get() = if (table.moduleName.isNotBlank()) table.moduleName else extension.moduleName

    protected open val projectName: String
        get() = extension.projectName

    protected open val applicationName: String
        get() = extension.applicationName

    protected val settings: Map<String, String>
        get() = extension.settings

    protected open val Column.jsonViewIgnored: Boolean
        get() = jsonViewIgnored(extension)

    protected open val Column.isSoftDelete: Boolean
        get() = isSoftDelete(extension)

    protected open val Table.supportSoftDelete: Boolean
        get() = supportSoftDelete(extension)

    protected fun setting(key: String): Any? = settings[key]

    protected fun setting(key: String, default: String): String {
        return settings[key] ?: return default
    }

    protected fun enable(key: String, default: Boolean = true): Boolean {
        return setting(key, default.toString()) == "true"
    }

    protected open val className
        get() = table.className(extension)

    protected open val projectClassName
        get() = if (enable("projectClassName", true)) className + projectName.substring(
            0, if (projectName.length > 5) 5 else projectName.length
        ).capitalize() else className

    protected val entityName
        get() = table.entityName(extension)

    protected val projectEntityName
        get() = projectClassName.decapitalize()

    /**
     * 表名
     */
    protected val tableName: String
        get() = table.tableName

    protected val catalog: String?
        get() = table.catalog

    protected val schema: String?
        get() = table.schema

    /**
     * 表类型
     */
    protected val tableType: String
        get() = table.tableType

    /**
     * 注释说明
     */
    protected val remarks: String
        get() {
            val comment =
                if (table.remarks.endsWith("表")) table.remarks.substringBeforeLast("表") else table.remarks
            return if (comment.isBlank()) {
                extension.remarks
            } else {
                comment
            }
        }

    /**
     * 主键
     */
    protected val primaryKeys: List<Column>
        get() {
            val primaryKeys = table.primaryKeys
            return if (primaryKeys.isEmpty()) {
                val primaryKey =
                    columns.find { it.columnName.equals(extension.primaryKeyName, true) }
                        ?: columns.find { it.remarks.contains("主键") }
                if (primaryKey != null) {
                    listOf(primaryKey)
                } else {
                    emptyList()
                }
            } else {
                primaryKeys
            }
        }

    /**
     * 主键
     */
    protected val primaryKey: Column
        get() {
            if (primaryKeys.size == 1) {
                return primaryKeys[0]
            } else {
                throw GeneratorException("$tableName:没有单主键，$primaryKeyNames")
            }
        }

    /**
     * 是否组合主键
     */
    protected val compositePrimaryKey: Boolean
        get() = primaryKeys.size > 1

    /**
     * 非主键字段
     */
    protected val otherColumns: List<Column>
        get() = columns.filter { !primaryKeys.contains(it) }

    /**
     * 字段
     */
    protected val columns: List<Column>
        get() = table.columns

    protected val indexes: List<Indexed>
        get() = table.indexes

    private val primaryKeyNames: List<String>
        get() = table.primaryKeyNames

    protected val pathName: String
        get() = table.pathName(extension)

    fun call(extension: GeneratorExtension, table: Table): Any? {
        this.extension = extension
        this.table = table
        return if (extension.delete) {
            if (destFile.delete()) {
                println("删除：${destFile.absolutePath.substringAfter(basePath.absolutePath + File.separator)}")
            }
            null
        } else {
            if (supports())
                doCall()
            else
                null
        }
    }

    protected open fun supports(): Boolean {
        return true
    }

    protected open fun output(printWriter: PrintWriter) {

    }

    fun setUp(extension: GeneratorExtension) {
        this.extension = extension
        setUp()
    }

    fun tearDown(extension: GeneratorExtension) {
        this.extension = extension
        tearDown()
    }

    open fun setUp() {

    }

    open fun tearDown() {

    }

    protected open fun doCall() {
        if (destFile.exists() && ((!extension.replaceAll && !cover) || destFile.readLines()
                .any { it.contains("[[Don't cover]]") })
        ) {
            return
        }
        destFile.parentFile.mkdirs()

        val msg = if (destFile.exists()) "覆盖" else "生成"
        destFile.printWriter().use {
            output(it)
        }
        println("$msg：${destFile.absolutePath.substringAfter(basePath.absolutePath + File.separator)}")
    }
}