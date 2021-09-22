package top.bettercode.generator.database

import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import top.bettercode.generator.GeneratorExtension
import java.io.File

/**
 * @author Peter Wu
 */
class OracleMetaDataTest {
    private val extension = GeneratorExtension(
        debug = true,
        basePath = File("build"),
        dir = "src/test/resources",
        packageName = "com.bettercode.test",
        tableNames = arrayOf("PU_CUST_TASK_LIST")
    )

    init {
        extension.datasource.url = "jdbc:oracle:thin:@10.0.1.101:1521:hollycrm"
        extension.datasource.username = "yuntudev"
        extension.datasource.password = "yuntudev"
    }

    @BeforeEach
    fun setUp() {
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
