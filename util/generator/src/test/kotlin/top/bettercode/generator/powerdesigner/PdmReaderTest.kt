package top.bettercode.generator.powerdesigner

import org.junit.jupiter.api.Test
import top.bettercode.generator.GeneratorExtension.Companion.defaultModuleName
import top.bettercode.generator.ddl.MysqlToDDL
import top.bettercode.generator.dom.unit.FileUnit
import java.io.File

/**
 * @author Peter Wu
 */
class PdmReaderTest {
    private val pdmFile = File(
        PdmReaderTest::class.java.getResource("/pdm.src/kie.pdm")?.file
            ?: throw IllegalStateException()
    )

    @Test
    fun read() {
        for (table in PdmReader.read(
            pdmFile
        )) {
            println(table)
        }
    }

    @Test
    fun toDDL() {
        val out = FileUnit("build/gen/puml/mysql.sql")
        MysqlToDDL.toDDL(
            PdmReader.read(
                pdmFile
            ), out
        )
        out.writeTo()
    }
}