package cn.bestwu.generator.database

import cn.bestwu.generator.DataType
import cn.bestwu.generator.GeneratorExtension
import cn.bestwu.generator.dsl.Generators
import cn.bestwu.generator.puml.PumlConverterTest
import org.junit.jupiter.api.Test
import java.io.File

/**
 *
 * @author Peter Wu
 */
class PumlGeneratorsTest {
    private val extension = GeneratorExtension(basePath = File("build"), dir = "gen/java", packageName = "com.bestwu.test", replaceAll = true, tablePrefix = "OAUTH_", dataType = DataType.PUML, tableNames = arrayOf("OAUTH_CLIENT_DETAILS", "OAUTH_CLIENT_TOKEN"))

    @Test
    fun gen() {
        extension.generators = arrayOf(
        )
        extension.pumlSrc = PumlConverterTest::class.java.getResource("/database.puml").file
        Generators.call(extension)
    }
}