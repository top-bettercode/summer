package top.bettercode.generator

import kotlinx.coroutines.async
import kotlinx.coroutines.runBlocking
import top.bettercode.generator.GeneratorExtension.Companion.defaultModuleName
import top.bettercode.generator.database.DatabaseMetaData
import top.bettercode.generator.database.entity.Table
import java.sql.Connection
import java.sql.DriverManager
import java.util.*
import java.util.concurrent.ConcurrentHashMap

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

    inline fun <T> use(metaData: DatabaseMetaData.() -> T): T {
        if (available) {
            Class.forName(driverClass).getConstructor().newInstance()
            val databaseMetaData = DatabaseMetaData(module, this)
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
        val names = tableNames.distinct()
        val size = names.size
        println("数据表（$size）:${names}")
        val resultMap = ConcurrentHashMap<String, Table>()
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
                    (names.filter {
                        !resultMap.keys().toList().contains(it)
                    })
                }表"
            )
        }
        return names.mapNotNull { resultMap[it] }.toList()
    }

}
