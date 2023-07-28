package top.bettercode.summer.tools.generator.database

import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import top.bettercode.summer.tools.generator.GeneratorExtension
import top.bettercode.summer.tools.generator.GeneratorExtension.Companion.defaultModuleName
import top.bettercode.summer.tools.generator.JDBCConnectionConfiguration
import top.bettercode.summer.tools.generator.dsl.Generators
import java.io.File

/**
 * @author Peter Wu
 */
class MysqlMetaDataTest {
    private val extension = GeneratorExtension(
            projectDir = File("build"),
            dir = "src/test/resources",
            packageName = "com.bettercode.test",
            tableNames = arrayOf("ob_chat_msg")
    )

    init {
        val configuration = JDBCConnectionConfiguration()
        configuration.url = ""
        configuration.username = ""
        configuration.password = ""
        configuration.debug = true
        extension.datasources = mapOf(defaultModuleName to configuration)
    }

    @BeforeEach
    fun setUp() {
    }

    @Test
    fun tableNames() {
        println(Generators.tableNames(extension))
        println(extension.defaultDatasource.tableNames())
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
