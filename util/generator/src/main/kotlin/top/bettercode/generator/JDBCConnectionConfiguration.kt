package top.bettercode.generator

import kotlinx.coroutines.async
import kotlinx.coroutines.runBlocking
import top.bettercode.generator.GeneratorExtension.Companion.defaultModuleName
import top.bettercode.generator.database.DatabaseMetaData
import top.bettercode.generator.database.entity.Table
import java.sql.Connection
import java.sql.DriverManager
import java.util.*
import java.util.concurrent.ConcurrentLinkedDeque
import java.util.concurrent.ConcurrentSkipListSet

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
        set("tinyInt1isBit", "false")
    }
) : TableHolder {
    val available: Boolean by lazy { url.isNotBlank() }

    lateinit var ext: GeneratorExtension

    var schema: String? = null
        get() {
            return if (field.isNullOrBlank()) {
                when {
                    isOracle -> username.uppercase(Locale.getDefault())
                    databaseDriver == DatabaseDriver.H2 -> "PUBLIC"
                    else -> field
                }
            } else {
                field
            }
        }

    val isOracle by lazy { databaseDriver == DatabaseDriver.ORACLE }

    val databaseDriver by lazy { DatabaseDriver.fromJdbcUrl(url) }

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
        val size = tableName.size
        val set = ConcurrentSkipListSet(tableName.toSet())
        val names = if (tableName.isEmpty()) tableNames() else tableName.distinct()
        println("数据表（$size）:${names}")
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
                                    table.ext = ext
                                    table.datasource = this@JDBCConnectionConfiguration
                                    table.module = module
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
