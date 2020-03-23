package cn.bestwu.generator.dsl

import cn.bestwu.generator.GeneratorException
import cn.bestwu.generator.GeneratorExtension
import cn.bestwu.generator.database.domain.Column
import cn.bestwu.generator.database.domain.Indexed
import cn.bestwu.generator.database.domain.Table
import java.io.File
import java.io.PrintWriter

/**
 * 模板基类
 */
abstract class Generator {
    companion object {
        const val DEFAULT_NAME = "generated.txt"

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
                                    ch0 == 't' || ch0 == 'T' || ch0 == '1') {
                                return true
                            }
                            if (ch0 == 'n' || ch0 == 'N' ||
                                    ch0 == 'f' || ch0 == 'F' || ch0 == '0') {
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
                                    (ch2 == 's' || ch2 == 'S')) {
                                return true
                            }
                            if ((ch0 == 'o' || ch0 == 'O') &&
                                    (ch1 == 'f' || ch1 == 'F') &&
                                    (ch2 == 'f' || ch2 == 'F')) {
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
                                    (ch3 == 'e' || ch3 == 'E')) {
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
                                    (ch4 == 'e' || ch4 == 'E')) {
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

    protected val Column.defaultRemarks: String
        get() = defaultDesc.replace("DEFAULT ", "默认值：")
    protected lateinit var table: Table
    protected lateinit var extension: GeneratorExtension
    protected val isOracleDatasource
        get() = extension.datasource.isOracle

    protected val basePackageName: String
        get() = extension.basePackageName

    open var cover: Boolean = false
    protected open val test: Boolean = false

    protected open val dir: String
        get() = if (test) extension.dir.replace("src/main/", "src/test/") else extension.dir

    protected val basePath: File
        get() = extension.basePath

    protected open val path: File
        get() = File(basePath, dir)

    /**
     * 文件名称
     */
    protected open val name: String = DEFAULT_NAME

    protected open val destFile: File
        get() {
            return File(path, if (this is JavaGenerator) "${name.replace(".", "/")}.java" else name)
        }

    open var module: String = ""
        get() = if (extension.module.isNotBlank()) extension.module else field

    open val moduleName: String
        get() = if (table.moduleName.isNotBlank()) table.moduleName else extension.moduleName

    protected open val projectName: String
        get() = extension.projectName

    protected open val applicationName: String
        get() = extension.applicationName

    protected val settings: Map<String, Any?>
        get() = extension.settings

    protected open val Column.jsonViewIgnored: Boolean
        get() = jsonViewIgnored(extension)

    protected open val Column.isSoftDelete: Boolean
        get() = isSoftDelete(extension)

    protected open val Table.supportSoftDelete: Boolean
        get() = supportSoftDelete(extension)

    protected fun property(key: String): Any? = settings[key]

    protected fun <T> property(key: String, default: T): T {
        val any = settings[key]
        return if (any == null) {
            return default
        } else {
            @Suppress("UNCHECKED_CAST")
            any as T
        }
    }

    protected open val className
        get() = table.className(extension)

    protected val entityName
        get() = table.entityName(extension)

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
            val comment = if (table.remarks.endsWith("表")) table.remarks.substringBeforeLast("表") else table.remarks
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
                val primaryKey = columns.find { it.columnName.equals(extension.primaryKeyName, true) }
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
     * 字段
     */
    protected val columns: List<Column>
        get() = table.columns

    protected val indexes: List<Indexed>
        get() = table.indexes

    protected val primaryKeyNames: List<String>
        get() = table.primaryKeyNames

    protected val pathName: String
        get() = table.pathName(extension)

    fun call(extension: GeneratorExtension, table: Table): Any? {
        this.extension = extension
        this.table = table
        return if (extension.delete) {
            if (destFile.delete()) {
                println("删除：${destFile.absolutePath.substringAfter(basePath.absolutePath + "/")}")
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
        if (destFile.exists() && ((!extension.replaceAll && !cover) || destFile.readLines().any { it.contains("[[Don't cover]]") })) {
            return
        }
        destFile.parentFile.mkdirs()

        val msg = if (destFile.exists()) "覆盖" else "生成"
        destFile.printWriter().use {
            output(it)
        }
        println("$msg：${destFile.absolutePath.substringAfter(basePath.absolutePath + "/")}")
    }
}