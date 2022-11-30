package top.bettercode.generator.database

import org.h2.jdbcx.JdbcDataSource
import org.h2.tools.RunScript
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import top.bettercode.generator.DataType
import top.bettercode.generator.GeneratorExtension
import top.bettercode.generator.GeneratorExtension.Companion.defaultModuleName
import top.bettercode.generator.JDBCConnectionConfiguration
import top.bettercode.generator.dsl.Generators
import top.bettercode.generator.dsl.def.PlantUML
import java.io.File
import java.io.FileReader

/**
 *
 * @author Peter Wu
 */
class GeneratorsTest {
    private val extension = GeneratorExtension(
        projectDir = File("build/resources/test/"),
        dir = "gen/java",
        packageName = "com.bettercode.test",
        replaceAll = true,
    )

    init {
        val configuration = JDBCConnectionConfiguration()
        configuration.url = "jdbc:h2:mem:test"
        configuration.username = "sa"
        configuration.password = "sa"
        configuration.tablePrefixes = arrayOf("oauth_")
        extension.datasources = mapOf(defaultModuleName to configuration)
    }

    @Test
    fun gen() {
        extension.generators = arrayOf(
            PlantUML(null, File("build/gen/puml/database.puml"), null)
        )
        extension.dataType = DataType.PDM
        Generators.call(extension)
    }

    @Test
    fun tableNames() {
        extension.dataType = DataType.PDM
        print(
            "============>" + Generators.tableNames(extension).joinToString(",") + "<============"
        )
    }

    companion object {
        @JvmStatic
        @BeforeAll
        fun setUp() {
            val jdbcDataSource = JdbcDataSource()
            jdbcDataSource.setURL("jdbc:h2:mem:test")
            jdbcDataSource.user = "sa"
            jdbcDataSource.password = "sa"
            RunScript.execute(
                jdbcDataSource.connection,
                FileReader(
                    MetaDataTest::class.java.getResource("/hsql.sql")?.file
                        ?: throw IllegalStateException("文件不存在")
                )
            )
        }
    }
}