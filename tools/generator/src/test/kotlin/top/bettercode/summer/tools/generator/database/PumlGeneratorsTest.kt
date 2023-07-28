package top.bettercode.summer.tools.generator.database

import org.junit.jupiter.api.Test
import top.bettercode.summer.tools.generator.GeneratorExtension
import top.bettercode.summer.tools.generator.dsl.Generators
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
            dataType = top.bettercode.summer.tools.generator.DataType.PUML,
            tableNames = arrayOf("OAUTH_CLIENT_DETAILS", "OAUTH_CLIENT_TOKEN")
    )

    @Test
    fun gen() {
        extension.generators = arrayOf(
        )
        Generators.callInAllModule(extension)
    }
}