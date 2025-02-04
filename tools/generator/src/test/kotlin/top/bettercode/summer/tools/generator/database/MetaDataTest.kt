package top.bettercode.summer.tools.generator.database

import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import top.bettercode.summer.tools.generator.GeneratorExtension
import top.bettercode.summer.tools.generator.GeneratorExtension.Companion.DEFAULT_MODULE_NAME
import top.bettercode.summer.tools.generator.DatabaseConfiguration
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
        val configuration = DatabaseConfiguration()
        configuration.url = "jdbc:h2:mem:test"
        configuration.username = "sa"
        configuration.password = "sa"
        extension.databases = mapOf(DEFAULT_MODULE_NAME to configuration)
    }

    @Test
    fun remarks() {
        val s="计算方式：YUAN_TON(\"元/吨\"),\n" +
                "YUAN_CAR(\"元/车),\n" +
                "YUAN_SQUARE(\"元/方\");"
        System.err.println(s.replace(Regex("[\t\n\r]"), ""))
        Assertions.assertEquals("计算方式：YUAN_TON(\"元/吨\"),YUAN_CAR(\"元/车),YUAN_SQUARE(\"元/方\");",s.replace(Regex("[\t\n\r]"), ""))
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
