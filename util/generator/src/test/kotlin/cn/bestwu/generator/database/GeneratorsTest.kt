package cn.bestwu.generator.database

import cn.bestwu.generator.DataType
import cn.bestwu.generator.GeneratorExtension
import cn.bestwu.generator.dsl.Generators
import cn.bestwu.generator.dsl.def.PlantUML
import org.h2.jdbcx.JdbcDataSource
import org.h2.tools.RunScript
import org.junit.Before
import org.junit.Test
import java.io.File
import java.io.FileReader

/**
 *
 * @author Peter Wu
 */
class GeneratorsTest {
    private val extension = GeneratorExtension(basePath = File("build/resources/test/"), dir = "gen/java", packageName = "com.bestwu.test", replaceAll = true, tablePrefix = "oauth_", pdmSrc = "kie.pdm")

    init {
        extension.datasource.url = "jdbc:h2:mem:test"
        extension.datasource.username = "sa"
        extension.datasource.password = "sa"

//        extension.tableNames = arrayOf("OAUTH_CLIENT_DETAILS", "OAUTH_CLIENT_TOKEN")
    }

    @Before
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
                PlantUML(null, "build/gen/puml/database.puml")
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