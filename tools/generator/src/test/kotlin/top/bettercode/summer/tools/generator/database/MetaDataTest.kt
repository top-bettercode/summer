package top.bettercode.summer.tools.generator.database

import org.junit.jupiter.api.Test
import top.bettercode.summer.tools.generator.GeneratorExtension
import top.bettercode.summer.tools.generator.GeneratorExtension.Companion.defaultModuleName
import top.bettercode.summer.tools.generator.JDBCConnectionConfiguration
import java.io.File

/**
 * @author Peter Wu
 */
class MetaDataTest {
    private val extension = GeneratorExtension(
            projectDir = File("build"),
            dir = "src/test/resources",
            packageName = "com.bettercode.test",
            tableNames = arrayOf("OAUTH_CLIENT_DETAILS", "OAUTH_CLIENT_TOKEN")
    )

    init {
        val configuration = JDBCConnectionConfiguration()
        configuration.url = "jdbc:h2:mem:test"
        configuration.username = "sa"
        configuration.password = "sa"
        extension.datasources = mapOf(defaultModuleName to configuration)
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
