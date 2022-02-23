package top.bettercode.generator.database

import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import top.bettercode.generator.GeneratorExtension
import top.bettercode.generator.JDBCConnectionConfiguration
import top.bettercode.generator.defaultModuleName
import java.io.File

/**
 * @author Peter Wu
 */
class MysqlMetaDataTest {
    private val extension = GeneratorExtension(
        basePath = File("build"),
        dir = "src/test/resources",
        packageName = "com.bettercode.test",
        tableNames = arrayOf("ob_chat_msg")
    )

    init {
        val configuration = JDBCConnectionConfiguration()
        configuration.url = ""
        configuration.username = ""
        configuration.password = ""
        extension.datasources = mapOf(defaultModuleName to configuration)
    }

    @BeforeEach
    fun setUp() {
//        val jdbcDataSource = JdbcDataSource()
//        jdbcDataSource.setURL("jdbc:h2:mem:test")
//        jdbcDataSource.user = "sa"
//        jdbcDataSource.password = "sa"
//        RunScript.execute(jdbcDataSource.connection, FileReader(MetaDataTest::class.java.getResource("/hsql.sql").file))
    }

    @Test
    fun tableNames() {
        println(extension.defaultDatasource.use { tableNames() })
    }

    @Test
    fun table() {
        extension.tableNames.forEach {
            val table = extension.defaultDatasource.use { table(it) }
            println(table)
            println(table?.indexes?.joinToString("\n\n"))
        }
    }

}
