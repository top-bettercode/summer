package top.bettercode.generator.database

import top.bettercode.generator.DataType
import top.bettercode.generator.GeneratorExtension
import top.bettercode.generator.dsl.Generators
import top.bettercode.generator.dsl.def.PlantUML
import org.h2.jdbcx.JdbcDataSource
import org.h2.tools.RunScript
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.io.File
import java.io.FileReader

/**
 *
 * @author Peter Wu
 */
class GeneratorsTest {
    private val extension = GeneratorExtension(basePath = File("build/resources/test/"), dir = "gen/java", packageName = "com.bettercode.test", replaceAll = true, tablePrefixes = arrayOf("oauth_"), pdmSrc = "kie.pdm")

    init {
        extension.datasource.url = "jdbc:h2:mem:test"
        extension.datasource.username = "sa"
        extension.datasource.password = "sa"

//        extension.tableNames = arrayOf("OAUTH_CLIENT_DETAILS", "OAUTH_CLIENT_TOKEN")
    }

    @BeforeEach
    fun setUp() {
        val jdbcDataSource = JdbcDataSource()
        jdbcDataSource.setURL("jdbc:h2:mem:test")
        jdbcDataSource.user = "sa"
        jdbcDataSource.password = "sa"
        RunScript.execute(jdbcDataSource.connection, FileReader(MetaDataTest::class.java.getResource("/hsql.sql").file))
    }

    @Test
    fun gen() {
        extension.generators = arrayOf(
                PlantUML(null, "build/gen/puml/database.puml", null)
        )
        extension.dataType = DataType.PDM
        Generators.call(extension)
    }

    @Test
    fun tableNames() {
        extension.dataType = DataType.PDM
        print("============>" + Generators.tableNames(extension).joinToString(",") + "<============")
    }
}