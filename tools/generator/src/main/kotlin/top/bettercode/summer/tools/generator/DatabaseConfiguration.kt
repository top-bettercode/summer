package top.bettercode.summer.tools.generator

import kotlinx.coroutines.async
import kotlinx.coroutines.runBlocking
import top.bettercode.summer.tools.generator.GeneratorExtension.Companion.DEFAULT_MODULE_NAME
import top.bettercode.summer.tools.generator.GeneratorExtension.Companion.javaName
import top.bettercode.summer.tools.generator.database.DatabaseMetaData
import top.bettercode.summer.tools.generator.database.entity.Table
import java.sql.Connection
import java.sql.DriverManager
import java.util.*
import java.util.concurrent.ConcurrentLinkedDeque
import java.util.concurrent.ConcurrentSkipListSet

data class DatabaseConfiguration(
        var url: String = "",
        var catalog: String? = null,
        val properties: Properties = Properties().apply {
            set("remarksReporting", "true") //oracle 读取表注释
            set("useInformationSchema", "true")//mysql 读取表注释
            set("nullCatalogMeansCurrent", "true")//mysql 读取表
            set("characterEncoding", "utf8")
            set("user", "root")
            set("password", "root")
            set("tinyInt1isBit", "false")
        }
) : TableHolder {

    val available: Boolean by lazy { url.isNotBlank() }

    lateinit var extension: GeneratorExtension

    /**
     * 表前缀
     */
    var tablePrefixes: Array<String> = arrayOf()

    /**
     * 表后缀
     */
    var tableSuffixes: Array<String> = arrayOf()

    var entityPrefix: String = ""

    var debug: Boolean = false

    /**
     * 生成PUML时是否查询index，查询较耗时
     */
    var queryIndex: Boolean = true

    /**
     * 更新Sql时生成删除表语句
     */
    var dropTablesWhenUpdate: Boolean = false

    /**
     * 更新Sql时生成删除字段语句
     */
    var dropColumnsWhenUpdate: Boolean = false

    /**
     * 生成脚本是否包含Schema
     */
    var includeSchema: Boolean = true

    /**
     * 编码
     */
    var charset: String = "utf8mb4"

    /**
     * 编码排序
     */
    var collate: String = "utf8mb4_unicode_ci"

    /**
     * 排除表
     */
    var excludeTableNames: Array<String> = arrayOf()

    /**
     * 生成代码排除表
     */
    var excludeGenTableNames: Array<String> = arrayOf("api_token")

    /**
     * ClassName
     */
    fun className(tableName: String): String {
        return javaName(
                (if (entityPrefix.isBlank()) "" else entityPrefix + "_") + fixTableName(tableName), true
        )
    }

    fun fixTableName(tableName: String): String {
        var newName = tableName
        tablePrefixes.filter { tableName.startsWith(it) }.forEach {
            newName = newName.substringAfter(it)
        }
        tableSuffixes.filter { tableName.endsWith(it) }.forEach {
            newName = newName.substringBeforeLast(it)
        }
        return newName
    }

    var schema: String? = null
        get() {
            return if (field.isNullOrBlank()) {
                when {
                    isOracle -> username.uppercase(Locale.getDefault())
                    driver == DatabaseDriver.MYSQL -> {
                        val pattern1 = Regex("jdbc:mysql://[^/]*/(.*)?\\?.+")
                        if (url.matches(pattern1))
                            url.replace(
                                    pattern1,
                                    "$1"
                            ) else url.replace(
                                Regex("jdbc:mysql://[^/]*/(.*)"),
                                "$1"
                        )
                    }

                    driver == DatabaseDriver.H2 -> "PUBLIC"
                    else -> field
                }
            } else {
                field
            }
        }

    val isOracle by lazy { driver == DatabaseDriver.ORACLE }

    var driver: DatabaseDriver = DatabaseDriver.UNSET
        get() {
            return if (field == DatabaseDriver.UNSET && url.isNotBlank()) {
                DatabaseDriver.fromJdbcUrl(url)
            } else {
                field
            }
        }

    var driverClass: String = ""
        get() {
            return if (field.isBlank() && url.isNotBlank()) {
                driver.driverClassName ?: ""
            } else {
                field
            }

        }

    var module: String = DEFAULT_MODULE_NAME

    val isDefault: Boolean
        get() = module == DEFAULT_MODULE_NAME

    var username: String
        set(value) = properties.set("user", value)
        get() = properties.getProperty("user")
    var password: String by properties

    var tinyInt1isBit: Boolean
        set(value) = properties.set("tinyInt1isBit", value.toString())
        get() = properties.getProperty("tinyInt1isBit")?.toBoolean() ?: false

    inline fun <T> use(metaData: DatabaseMetaData.() -> T): T {
        if (available) {
            Class.forName(driverClass).getConstructor().newInstance()
            val databaseMetaData = DatabaseMetaData(this)
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


    override fun tableNames(): List<String> {
        return use {
            tableNames()
        }
    }

    override fun tables(checkFound: Boolean, vararg tableName: String): List<Table> {
        val set = ConcurrentSkipListSet(tableName.toSet())
        val names = (if (tableName.isEmpty()) tableNames() else tableName.distinct()).filter {
            !excludeTableNames.contains(it)
        }
        println("数据表（${names.size}）:${names}")
        val result = ConcurrentLinkedDeque<Table>()
        val map = mutableMapOf<Int, MutableList<String>>()
        var i = 1
        names.forEach {
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
                                val table = table(it) { table ->
                                    table.database = this@DatabaseConfiguration
                                }
                                if (table != null) {
                                    set.remove(table.tableName)
                                    table
                                } else {
                                    null
                                }
                            } catch (e: Exception) {
                                e.printStackTrace()
                                null
                            }
                        }
                    }
                }
            }
            result.addAll(deferred.flatMap { it.await() }.filterNotNull())
        }
        if (checkFound && set.isNotEmpty()) {
            System.err.println("未找到${set}表")
        }
        return result.sortedBy { it.tableName }
    }

}
