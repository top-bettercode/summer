package top.bettercode.summer.tools.generator.database

import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import top.bettercode.summer.tools.generator.GeneratorExtension
import top.bettercode.summer.tools.generator.GeneratorExtension.Companion.DEFAULT_MODULE_NAME
import top.bettercode.summer.tools.generator.DatabaseConfiguration
import java.io.File

/**
 * @author Peter Wu
 */
@Disabled
class OracleMetaDataTest {
    private val extension = GeneratorExtension(
            projectDir = File("build"),
            dir = "src/test/resources",
            packageName = "com.bettercode.test",
            tableNames = arrayOf("PU_CUST_TASK_LIST")
    )

    init {
        val configuration = DatabaseConfiguration()
        configuration.debug = true
        configuration.url = ""
        configuration.username = ""
        configuration.password = ""
        extension.databases = mapOf(DEFAULT_MODULE_NAME to configuration)
    }

    @BeforeEach
    fun setUp() {
    }

    @Test
    fun tableNames() {
        println(extension.databases.values.first().use { tableNames() })
    }


    @Test
    fun table() {
        extension.tableNames.forEach {
            val table = extension.databases.values.first().use { table(it) }
            println(table)
            println(table?.indexes?.joinToString("\n\n"))
        }
    }

}
