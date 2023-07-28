package top.bettercode.summer.tools.generator.powerdesigner

import org.junit.jupiter.api.Test
import java.io.File

/**
 * @author Peter Wu
 */
class PdmReaderTest {
    private val pdmFile = File(
            PdmReaderTest::class.java.getResource("/pdm/src/kie.pdm")?.file
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

}