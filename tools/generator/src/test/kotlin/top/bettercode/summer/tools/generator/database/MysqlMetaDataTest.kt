package top.bettercode.summer.tools.generator.database

import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import top.bettercode.summer.tools.generator.GeneratorExtension
import top.bettercode.summer.tools.generator.GeneratorExtension.Companion.DEFAULT_MODULE_NAME
import top.bettercode.summer.tools.generator.DatabaseConfiguration
import top.bettercode.summer.tools.generator.dsl.Generators
import java.io.File

/**
 * @author Peter Wu
 */
@Disabled
class MysqlMetaDataTest {
    private val extension = GeneratorExtension(
            projectDir = File("build"),
            dir = "src/test/resources",
            packageName = "com.bettercode.test",
            tableNames = arrayOf("ob_chat_msg")
    )

    init {
        val configuration = DatabaseConfiguration()
        configuration.url = ""
        configuration.username = ""
        configuration.password = ""
        configuration.debug = true
        extension.databases = mapOf(DEFAULT_MODULE_NAME to configuration)
    }

    @BeforeEach
    fun setUp() {
    }

    @Test
    fun tableNames() {
        println(Generators.tableNames(extension))
        println(extension.databases.values.first().tableNames())
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
