package cn.bestwu.generator

import cn.bestwu.generator.database.DatabaseMetaData
import cn.bestwu.generator.database.entity.Table
import cn.bestwu.generator.dom.java.element.JavaElement
import cn.bestwu.generator.dsl.Generator
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import kotlinx.coroutines.runBlocking
import java.io.File
import java.sql.Connection
import java.sql.DriverManager
import java.util.*
import java.util.concurrent.ConcurrentHashMap

enum class DataType {
    DATABASE,
    PUML,
    PDM
}

/**
 * @author Peter Wu
 */
open class GeneratorExtension(
    /**
     * JDBC连接配置
     */
    val datasource: JDBCConnectionConfiguration = JDBCConnectionConfiguration(),
    /**
     * 单数据源
     */
    var singleDatasource: Boolean = true,
    var debug: Boolean = false,
    /**
     * 包名是否自动加项目名区分
     */
    var projectPackage: Boolean = false,

    /**
     * 生成文件基础路径,项目路径
     */
    var basePath: File = File("").absoluteFile,
    /**
     * 基础路径下相对路径
     */
    var dir: String = "",

    /**
     * 数据源类型，默认数据库
     */
    var dataType: DataType = DataType.DATABASE,
    /**
     * PlantUML 脚本目录
     */
    var pumlSrc: String = "puml/src",
    /**
     * pdm文件路径
     */
    var pdmSrc: String = "database/database.pdm",
    var pumlDatabaseDriver: DatabaseDriver = datasource.databaseDriver,
    var pumlDatabase: String = "puml/database",
    /**
     * PlantUML 图片类型
     */
    var pumlDiagramFormat: String = "PNG",
    /**
     * SQL 脚本目录
     */
    var sqlOutput: String = "database",
    var sqlQuote: Boolean = true,
    /**
     * 覆盖所有已生成文件
     */
    var replaceAll: Boolean = true,
    /**
     * 删除模式，为true时不生成文件，删除已生成的文件
     */
    var delete: Boolean = false,
    /**
     * 生成SQL时是否生成外键相关语句
     */
    var useForeignKey: Boolean = false,
    /**
     * 生成代码包名
     */
    var packageName: String = "",
    /**
     * 使用子模块
     */
    var userModule: Boolean = true,
    /**
     * 更新Sql时生成删除表语句
     */
    var deleteTablesWhenUpdate: Boolean = false,
    /**
     * SQL更新时，根据什么更新
     */
    var updateFromType: DataType = DataType.DATABASE,
    /**
     * 子模块
     */
    var module: String = "",
    /**
     * 子模块名称
     */
    var moduleName: String = "",

    var applicationName: String = "",

    var projectName: String = "",
    /**
     * 表前缀
     */
    var tablePrefixes: Array<String> = arrayOf(),
    /**
     * 注释说明
     */
    var remarks: String = "",
    /**
     * 手动主键名
     */
    var primaryKeyName: String = "id",
    /**
     * 逻辑删除字段名
     */
    var softDeleteColumnName: String = "deleted",
    /**
     * 逻辑删除使用布尔值
     */
    var softDeleteAsBoolean: Boolean = true,
    /**
     * 缩进
     */
    var indent: String = JavaElement.defaultIndent,
    /**
     * 模板
     */
    var generators: Array<Generator> = arrayOf(),
    /**
     * 相关数据表
     */
    var tableNames: Array<String> = arrayOf(),
    var pumlTableNames: Array<String> = arrayOf(),
    /**
     * 额外设置
     */
    var settings: MutableMap<String,String> = mutableMapOf()
) {

    companion object {
        /**
         * javaName
         */
        var javaName: (String) -> String = {
            javaName(it, false)
        }

        private fun javaName(str: String, capitalize: Boolean = false): String {
            val s = str.split(Regex("[^\\p{Alnum}]")).joinToString("") {
                it.toLowerCase().capitalize()
            }
            return if (capitalize) s else s.decapitalize()
        }

    }

    /**
     * 根路径
     */
    var rootPath: File? = null
        get() = if (field == null) {
            findUpPath(basePath)
        } else field

    private val path: File?
        get() {
            return if (File(basePath, pumlSrc).exists())
                basePath
            else rootPath
        }

    /**
     * json 序列化过滤字段
     */
    var jsonViewIgnoredFieldNames: Array<String> = arrayOf()

    /**
     * ClassName
     */
    var className: (String) -> String = { str ->
        javaName(str.substringAfter(tablePrefixes.find { str.startsWith(it) } ?: ""), true)
    }

    val sqlDDLOutput: String
        get() = "$sqlOutput/ddl"

    private val sqlUpdateOutput: String
        get() = sqlOutput

    private fun findUpPath(file: File): File? {
        val parentFile = file.absoluteFile.parentFile
        return if (parentFile != null) {
            var pFile = File(parentFile, pumlSrc.substringBefore("/"))
            if (pFile.exists()) {
                parentFile
            } else {
                pFile = File(parentFile, pdmSrc)
                if (pFile.exists()) {
                    parentFile
                } else
                    findUpPath(parentFile)
            }
        } else
            null
    }

    fun <T> use(metaData: DatabaseMetaData.() -> T): T {
        Class.forName(datasource.driverClass).newInstance()
        val databaseMetaData = DatabaseMetaData(datasource, debug)
        try {
            return metaData(databaseMetaData)
        } finally {
            databaseMetaData.close()
        }
    }

    fun <T> run(connectionFun: Connection.() -> T): T {
        Class.forName(datasource.driverClass).newInstance()
        val connection = DriverManager.getConnection(datasource.url, datasource.properties)
        try {
            return connectionFun(connection)
        } finally {
            connection.close()
        }
    }

    fun tables(tableNames: Array<String>): List<Table> {
        val size = tableNames.size
        println("$size:${tableNames.joinToString()}")
        val resultMap = ConcurrentHashMap<String, Table>()
        val map = mutableMapOf<Int, MutableList<String>>()
        var i = 1
        tableNames.forEach {
            if (i > 10) {
                i = 1
            }
            map.computeIfAbsent(i) { mutableListOf() }.add(it)
            i++
        }
        val deferred = map.values.map {
            GlobalScope.async {
                use {
                    it.map {
                        try {
                            table(it)
                        } catch (e: Exception) {
                            e.printStackTrace()
                            null
                        }
                    }
                }
            }
        }
        runBlocking {
            resultMap.putAll(
                deferred.flatMap { it.await() }.filterNotNull().associateBy { it.tableName })
        }
        if (resultMap.size != size) {
            System.err.println(
                "未找到${
                    (tableNames.filter {
                        !resultMap.keys().toList().contains(it)
                    })
                }表"
            )
        }
        return tableNames.mapNotNull { resultMap[it] }.toList()
    }

    fun file(subfile: String): File {
        val file = File(subfile)
        if (file.isAbsolute)
            return file
        return File(path, subfile)
    }

    fun pumlSqlOutputFile(src: File, source: File): File {
        val dest = File(
            File(
                file(sqlDDLOutput),
                src.parentFile.absolutePath.replace(source.absolutePath, "")
            ), src.nameWithoutExtension + ".sql"
        )
        dest.parentFile.mkdirs()
        return dest
    }

    fun pumlSqlUpdateOutputFile(): File {
        val dest = File(file(sqlUpdateOutput), "update.sql")
        dest.parentFile.mkdirs()
        return dest
    }

    val pumlSrcSources: Sequence<File>
        get() {
            return file(pumlSrc).walkTopDown().filter { it.isFile && it.extension == "puml" }
        }

    val pumlAllSources: Sequence<File>
        get() {
            return file(pumlSrc).walkTopDown()
                .filter { it.isFile && it.extension == "puml" } + file(pumlDatabase).walkTopDown()
                .filter { it.isFile && it.extension == "puml" }
        }
}

class JDBCConnectionConfiguration(
    var url: String = "",
    var catalog: String? = null,
    val properties: Properties = Properties().apply {
        set("remarksReporting", "true") //oracle 读取表注释
        set("useInformationSchema", "true")//mysql 读取表注释
        set("nullCatalogMeansCurrent", "true")//mysql 读取表
        set("characterEncoding", "utf8")
        set("user", "root")
        set("password", "root")
    }
) {
    var schema: String? = null
        get() {
            return if (field.isNullOrBlank()) {
                when {
                    isOracle -> username.toUpperCase()
                    databaseDriver == DatabaseDriver.H2 -> "PUBLIC"
                    else -> field
                }
            } else {
                field
            }
        }

    val isOracle
        get() = databaseDriver == DatabaseDriver.ORACLE

    val databaseDriver
        get() = DatabaseDriver.fromJdbcUrl(url)

    var driverClass: String = ""
        get() {
            return if (field.isBlank() && !url.isBlank()) {
                databaseDriver.driverClassName ?: ""
            } else {
                field
            }

        }

    var username: String
        set(value) = properties.set("user", value)
        get() = properties.getProperty("user")
    var password: String by properties
}