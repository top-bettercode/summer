package top.bettercode.generator

import kotlinx.coroutines.async
import kotlinx.coroutines.runBlocking
import top.bettercode.generator.database.DatabaseMetaData
import top.bettercode.generator.database.entity.Table
import top.bettercode.generator.dom.java.element.JavaElement
import top.bettercode.generator.dsl.Generator
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

const val defaultModuleName = "modules"


/**
 * @author Peter Wu
 */
open class GeneratorExtension(
    /**
     * JDBC连接配置
     */
    var datasources: Map<String, JDBCConnectionConfiguration> = mapOf(),
    /**
     * 子项目共用数据源
     */
    var unitedDatasource: Boolean = true,
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
     * pdm文件路径
     */
    var pdmSrc: String = "pdm",
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

    var useJSR310Types: Boolean = true,
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
    var dropTablesWhenUpdate: Boolean = false,
    /**
     * 更新Sql时生成删除字段语句
     */
    var dropColumnsWhenUpdate: Boolean = false,
    /**
     * SQL更新时，根据什么更新
     */
    var updateFromType: DataType = DataType.DATABASE,

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
     * jpa id generator
     */
    var idgenerator: String = "",
    /**
     * 缩进
     */
    var indent: String = JavaElement.defaultIndent,
    /**
     * 模板
     */
    var generators: Array<Generator> = arrayOf(),

    /**
     * 公共状态码
     */
    var commonCodeTypes: Array<String> = arrayOf(),
    /**
     * 相关数据表
     */
    var tableNames: Array<String> = arrayOf(),
    /**
     * 额外设置
     */
    var settings: MutableMap<String, String> = mutableMapOf()
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

    val defaultDatasource: JDBCConnectionConfiguration by lazy { datasources[defaultModuleName]!! }

    /**
     * PlantUML 脚本目录
     */
    var pumlSrc: String = "puml"

    /**
     * 根路径
     */
    var rootPath: File? = null
        get() = if (field == null) {
            findUpPath(basePath)
        } else field

    private val path: File? by lazy {
        if (File(basePath, pumlSrc).exists())
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


    fun file(subfile: String): File {
        val file = File(subfile)
        if (file.isAbsolute)
            return file
        return File(path, subfile)
    }

    fun isDefaultModule(moduleName: String): Boolean {
        return "modules" == moduleName
    }

    val pumlSources: Map<String, List<File>> by lazy {
        file(pumlSrc).listFiles()?.filter { it.isDirectory && "database" != it.name }
            ?.associateBy(
                {
                    if ("src" == it.name) {
                        defaultModuleName
                    } else it.name
                }
            ) { it.walkTopDown().filter { f -> f.isFile && f.extension == "puml" }.toList() }
            ?: emptyMap()
    }

    val pdmSources: Map<String, List<File>> by lazy {
        file(pdmSrc).listFiles()?.filter { it.isDirectory && "database" != it.name }
            ?.associateBy(
                { if ("src" == it.name) "modules" else it.name }
            ) { it.walkTopDown().filter { f -> f.isFile && f.extension == "pdm" }.toList() }
            ?: emptyMap()
    }

    val pumlDatabaseSources: Map<String, List<File>> by lazy {
        file(pumlSrc).listFiles()?.filter { "database" == it.name }
            ?.associateBy(
                {
                    if ("src" == it.name) {
                        defaultModuleName
                    } else it.name
                }
            ) { it.walkTopDown().filter { f -> f.isFile && f.extension == "puml" }.toList() }
            ?: emptyMap()
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
    val available: Boolean by lazy { url.isNotBlank() }

    var schema: String? = null
        get() {
            return if (field.isNullOrBlank()) {
                when {
                    isOracle -> username.toUpperCase(Locale.getDefault())
                    databaseDriver == DatabaseDriver.H2 -> "PUBLIC"
                    else -> field
                }
            } else {
                field
            }
        }

    val isOracle by lazy { databaseDriver == DatabaseDriver.ORACLE}

    val databaseDriver by lazy {DatabaseDriver.fromJdbcUrl(url)}

    var driverClass: String = ""
        get() {
            return if (field.isBlank() && url.isNotBlank()) {
                databaseDriver.driverClassName ?: ""
            } else {
                field
            }

        }

    var module: String = defaultModuleName

    var username: String
        set(value) = properties.set("user", value)
        get() = properties.getProperty("user")
    var password: String by properties

    var debug: Boolean = false

    /**
     * 生成PUML时是否查询index，查询较耗时
     */
    var queryIndex: Boolean = true

    inline fun <T> use(metaData: DatabaseMetaData.() -> T): T {
        if (available) {
            Class.forName(driverClass).getConstructor().newInstance()
            val databaseMetaData = DatabaseMetaData(module, this, debug, queryIndex)
            try {
                return metaData(databaseMetaData)
            } finally {
                databaseMetaData.close()
            }
        } else {
            throw RuntimeException("数据库未配置")
        }
    }

    fun <T> run(connectionFun: Connection.() -> T): T {
        Class.forName(driverClass).getConstructor().newInstance()
        val connection = DriverManager.getConnection(url, properties)
        try {
            return connectionFun(connection)
        } finally {
            connection.close()
        }
    }

    fun tableNames(): List<String> {
        return use {
            tableNames()
        }
    }

    fun tables(tableNames: List<String>): List<Table> {
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

        runBlocking {
            val deferred = map.values.map {
                async {
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

}