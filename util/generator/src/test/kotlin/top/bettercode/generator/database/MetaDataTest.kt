package top.bettercode.generator.database

import top.bettercode.generator.GeneratorExtension
import org.h2.jdbcx.JdbcDataSource
import org.h2.tools.RunScript
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.io.File
import java.io.FileReader

/**
 * @author Peter Wu
 */
class MetaDataTest {
    private val extension = GeneratorExtension(basePath = File("build"), dir = "src/test/resources", packageName = "com.bettercode.test", tableNames = arrayOf("OAUTH_CLIENT_DETAILS", "OAUTH_CLIENT_TOKEN"))

    init {
        extension.datasource.url = "jdbc:h2:mem:test"
        extension.datasource.username = "sa"
        extension.datasource.password = "sa"
    }

    @BeforeEach
    fun setUp() {
        val jdbcDataSource = JdbcDataSource()
        jdbcDataSource.setURL("jdbc:h2:mem:test")
        jdbcDataSource.user = "sa"
        jdbcDataSource.password = "sa"
        RunScript.execute(jdbcDataSource.connection, FileReader(MetaDataTest::class.java.getResource("/hsql.sql").file))
    }

    @Test
    fun tableNames() {
        println(extension.use { tableNames() })
    }

    @Test
    fun table() {
        extension.tableNames.forEach {
            val table = extension.use { table(it) }
            println(table)
            println(table?.indexes?.joinToString("\n\n"))
        }
    }

}
