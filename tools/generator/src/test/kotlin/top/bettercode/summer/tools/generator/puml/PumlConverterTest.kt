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
    fun sequenceRegex() {
        var sequence = ""
        var sequenceStartWith = 0L
        val sequenceRegex = Regex(" SEQUENCE +(.+?)(\\(\\d+\\))? ")
        var extra = " SEQUENCE  SEQUENCE_TEST(10) "
//        var extra = " SEQUENCE  SEQUENCE_TEST "
        val match = sequenceRegex.find(extra)
        if (match != null) {
            val groupValues = match.groupValues
            sequence = groupValues[1]
            sequenceStartWith = groupValues[2].trim('(', ')').toLongOrNull() ?: 0L
            extra = extra.replace(sequenceRegex, " ")
        }
        System.err.println(extra)
        System.err.println(sequence)
        System.err.println(sequenceStartWith)
    }

    @Test
    fun convert() {
        val tables = PumlConverter.toTables(oraclePuml)
        println(tables)
    }

    @Test
    fun compile() {
        PumlConverter.compile(
            DatabaseConfiguration().apply { extension = GeneratorExtension() },
            oraclePuml,
            File("build/gen/puml/database.puml")
        )
    }

    @Test
    fun toMysql() {
        PumlConverter.toMysql(
            DatabaseConfiguration().apply { extension = GeneratorExtension() },
            oraclePuml,
            File("build/gen/puml/database.puml")
        )
    }

}