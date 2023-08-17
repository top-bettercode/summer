package top.bettercode.summer.tools.generator.puml

import org.junit.jupiter.api.Test
import top.bettercode.summer.tools.generator.DatabaseConfiguration
import top.bettercode.summer.tools.generator.GeneratorExtension
import java.io.File

/**
 * @author Peter Wu
 * @since 0.0.45
 */
class PumlConverterTest {
    val oraclePuml = File(
            PumlConverterTest::class.java.getResource("/puml/src/oracle.puml")?.file
                    ?: throw IllegalStateException()
    )


    @Test
    fun convert() {
        val tables = PumlConverter.toTables(oraclePuml)
        println(tables)
    }

    @Test
    fun compile() {
        PumlConverter.compile(
                DatabaseConfiguration().apply { extension= GeneratorExtension() },
                oraclePuml,
                File("build/gen/puml/database.puml")
        )
    }

    @Test
    fun toMysql() {
        PumlConverter.toMysql(
                DatabaseConfiguration().apply { extension= GeneratorExtension() },
                oraclePuml,
                File("build/gen/puml/database.puml")
        )
    }

}