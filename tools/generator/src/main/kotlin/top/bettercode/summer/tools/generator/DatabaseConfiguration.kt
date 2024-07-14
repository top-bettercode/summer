package top.bettercode.summer.tools.generator

import com.jcraft.jsch.JSch
import com.jcraft.jsch.Session
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.runBlocking
import top.bettercode.summer.tools.generator.GeneratorExtension.Companion.DEFAULT_MODULE_NAME
import top.bettercode.summer.tools.generator.GeneratorExtension.Companion.javaName
import top.bettercode.summer.tools.generator.database.DatabaseMetaData
import top.bettercode.summer.tools.generator.database.entity.Table
import top.bettercode.summer.tools.lang.decapitalized
import java.sql.Connection
import java.sql.DriverManager
import java.util.*
import java.util.concurrent.ConcurrentLinkedDeque
import java.util.concurrent.ConcurrentSkipListSet
import java.util.regex.Pattern

@Suppress("ConvertTryFinallyToUseCall")
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

    private val log = org.slf4j.LoggerFactory.getLogger(DatabaseConfiguration::class.java)

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
     * 权限使用数据库表
     */
    var storeToken: Boolean = true

    /**
     * 编码排序
     */
    var collate: String? = null

    /**
     * 排除表
     */
    var excludeTableNames: Array<String> = arrayOf()

    /**
     * 生成代码排除表
     */
    var excludeGenTableNames: Array<String> = arrayOf("api_token")

    var sshHost: String? = null
    var sshPort: Int = 22
    var sshUsername: String? = null
    var sshPassword: String? = null


    fun defaultCodeType(
        columnName: String,
        logicalDelete: Boolean,
        tableName: String,
    ): String {
        return if (columnName.contains("_") || logicalDelete || extension.commonCodeTypes.any {
                it.equals(
                    columnName,
                    true
                )
            })
            javaName(columnName)
        else
            className(tableName)
                .decapitalized() + javaName(
                columnName,
                true
            )
    }

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

    var offline: Boolean = false

    var module: String = DEFAULT_MODULE_NAME

    val isDefault: Boolean
        get() = GeneratorExtension.isDefaultModule(module)

    var username: String
        set(value) = properties.set("user", value)
        get() = properties.getProperty("user")
    var password: String by properties

    var tinyInt1isBit: Boolean
        set(value) = properties.set("tinyInt1isBit", value.toString())
        get() = properties.getProperty("tinyInt1isBit")?.toBoolean() ?: false

    fun sshProxy(run: () -> Unit) {
        if (available) {
            val session: Session? = if (!url.contains("localhost") && sshHost != null &&
                sshUsername != null && sshPassword != null
            ) {
                getDbHostPort(driver, url)?.let {
                    val jsch = JSch()
                    val session: Session = jsch.getSession(sshUsername, sshHost, 22)
                    session.setPassword(sshPassword)
                    val config = Properties()
                    config["StrictHostKeyChecking"] = "no"
                    session.setConfig(config)
                    session.connect()

                    // 建立SSH代理端口转发，将本地端口与数据库服务器建立连接
                    session.setPortForwardingL(it.second, it.first, it.second)
                    // 替换 host
                    url = url.replace(it.first, "localhost")
                    session
                }
            } else null
            try {
                run()
            } finally {
                session?.disconnect()
            }
        }
    }

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
        log.warn("数据表（${names.size}）:${names}")
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
            val customScope = CoroutineScope(Dispatchers.Default)
            val deferred = map.values.map {
                customScope.async {
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
                                log.error("表${it}查询失败", e)
                                null
                            }
                        }
                    }
                }
            }
            result.addAll(deferred.flatMap { it.await() }.filterNotNull())
        }
        if (checkFound && set.isNotEmpty()) {
            log.error("未找到${set}表")
        }
        return result.sortedBy { it.tableName }
    }

    companion object {
        fun getDbHostPort(driver: DatabaseDriver, dbUrl: String): Pair<String, Int>? {
            // 获取数据库主机名或 IP 地址
            val dbHostPortPattern = Pattern.compile(
                when (driver) {
                    DatabaseDriver.MYSQL ->
                        "jdbc:mysql://(.+?)(:(\\d+))?/.*"

                    DatabaseDriver.ORACLE ->
                        "jdbc:oracle:thin:@(.+?)(:(\\d+))?:.*"

                    else -> return null
                }
            )

            val defaultPort = when (driver) {
                DatabaseDriver.MYSQL -> 3306
                DatabaseDriver.ORACLE -> 1521
                else -> return null
            }

            val matcher = dbHostPortPattern.matcher(dbUrl)
            return if (matcher.find()) {
                val dbHost = matcher.group(1)
                val groupCount = matcher.groupCount()
                val dbPort = if (groupCount == 2) matcher.group(2)?.toInt()
                    ?: defaultPort else defaultPort
                Pair(dbHost, dbPort)
            } else {
                null
            }
        }

    }
}
