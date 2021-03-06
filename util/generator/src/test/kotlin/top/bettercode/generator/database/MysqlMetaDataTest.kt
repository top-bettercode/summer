package top.bettercode.generator.database

import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import top.bettercode.generator.GeneratorExtension
import top.bettercode.generator.GeneratorExtension.Companion.defaultModuleName
import top.bettercode.generator.JDBCConnectionConfiguration
import top.bettercode.generator.dsl.Generators
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
        configuration.url =
            "jdbc:mysql://10.0.60.207:3306/ytjp?characterEncoding=utf8&useSSL=false"
        configuration.username = "ytjp"
        configuration.password = "Ytjp_2019"
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
