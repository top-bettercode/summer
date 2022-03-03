package top.bettercode.generator.powerdesigner

import org.junit.jupiter.api.Test
import top.bettercode.generator.GeneratorExtension.Companion.defaultModuleName
import top.bettercode.generator.ddl.MysqlToDDL
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
            pdmFile,
            defaultModuleName
        )) {
            println(table)
        }
    }

    @Test
    fun toDDL() {
        MysqlToDDL.toDDL(
            PdmReader.read(
                pdmFile,
                defaultModuleName
            ), File("build/gen/puml/mysql.sql")
        )
    }
}