package top.bettercode.generator.database

import top.bettercode.generator.GeneratorExtension
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.io.File

/**
 * @author Peter Wu
 */
class MysqlMetaDataTest {
    private val extension = GeneratorExtension(basePath = File("build"), dir = "src/test/resources", packageName = "com.bettercode.test", tableNames = arrayOf("ob_chat_msg"))

    init {
        extension.datasource.url = ""
        extension.datasource.username = ""
        extension.datasource.password = ""
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
