package top.bettercode.generator.database

import org.junit.jupiter.api.Test
import top.bettercode.generator.DataType
import top.bettercode.generator.GeneratorExtension
import top.bettercode.generator.dsl.Generators
import java.io.File

/**
 *
 * @author Peter Wu
 */
class PumlGeneratorsTest {
    private val extension = GeneratorExtension(
        projectDir = File("build"),
        dir = "gen/java",
        packageName = "com.bettercode.test",
        replaceAll = true,
        dataType = DataType.PUML,
        tableNames = arrayOf("OAUTH_CLIENT_DETAILS", "OAUTH_CLIENT_TOKEN")
    )

    @Test
    fun gen() {
        extension.generators = arrayOf(
        )
        Generators.call(extension)
    }
}