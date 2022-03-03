package top.bettercode.generator.database

import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import top.bettercode.generator.GeneratorExtension
import top.bettercode.generator.GeneratorExtension.Companion.defaultModuleName
import top.bettercode.generator.JDBCConnectionConfiguration
import java.io.File

/**
 * @author Peter Wu
 */
class OracleMetaDataTest {
    private val extension = GeneratorExtension(
        projectDir = File("build"),
        dir = "src/test/resources",
        packageName = "com.bettercode.test",
        tableNames = arrayOf("PU_CUST_TASK_LIST")
    )

    init {
        val configuration = JDBCConnectionConfiguration()
        configuration.debug = true
        configuration.url = ""
        configuration.username = ""
        configuration.password = ""
        extension.datasources = mapOf(defaultModuleName to configuration)
    }

    @BeforeEach
    fun setUp() {
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
