package top.bettercode.summer.tools.generator.database

import org.h2.jdbcx.JdbcDataSource
import org.h2.tools.RunScript
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import top.bettercode.summer.tools.generator.GeneratorExtension
import top.bettercode.summer.tools.generator.GeneratorExtension.Companion.defaultModuleName
import top.bettercode.summer.tools.generator.JDBCConnectionConfiguration
import top.bettercode.summer.tools.generator.dsl.Generators
import top.bettercode.summer.tools.generator.dsl.def.PlantUML
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
            replaceAll = true
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
        extension.dataType = top.bettercode.summer.tools.generator.DataType.PDM
        Generators.callInAllModule(extension)
    }

    @Test
    fun tableNames() {
        extension.dataType = top.bettercode.summer.tools.generator.DataType.PDM
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