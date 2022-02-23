package top.bettercode.generator.database

import top.bettercode.generator.GeneratorExtension
import org.h2.jdbcx.JdbcDataSource
import org.h2.tools.RunScript
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import top.bettercode.generator.JDBCConnectionConfiguration
import top.bettercode.generator.defaultModuleName
import java.io.File
import java.io.FileReader

/**
 * @author Peter Wu
 */
class MetaDataTest {
    private val extension = GeneratorExtension(basePath = File("build"), dir = "src/test/resources", packageName = "com.bettercode.test", tableNames = arrayOf("OAUTH_CLIENT_DETAILS", "OAUTH_CLIENT_TOKEN"))

    init {
        val configuration = JDBCConnectionConfiguration()
        configuration.url = "jdbc:h2:mem:test"
        configuration.username = "sa"
        configuration.password = "sa"
        extension.datasources = mapOf(defaultModuleName to configuration)
    }

    @BeforeEach
    fun setUp() {
        val jdbcDataSource = JdbcDataSource()
        jdbcDataSource.setURL("jdbc:h2:mem:test")
        jdbcDataSource.user = "sa"
        jdbcDataSource.password = "sa"
        RunScript.execute(
            jdbcDataSource.connection,
            FileReader(
                MetaDataTest::class.java.getResource("/hsql.sql")?.file
                    ?: throw IllegalStateException("文件不存在")
            )
        )
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
