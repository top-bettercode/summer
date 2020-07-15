package cn.bestwu.generator.powerdesigner

import cn.bestwu.generator.ddl.MysqlToDDL
import org.junit.jupiter.api.Test
import java.io.File

/**
 * @author Peter Wu
 */
class PdmReaderTest {

    @Test
    fun read() {
        for (table in PdmReader.read(File(PdmReaderTest::class.java.getResource("/kie.pdm").file))) {
            println(table)
        }
    }

    @Test
    fun toDDL() {
        MysqlToDDL.toDDL(PdmReader.read(File(PdmReaderTest::class.java.getResource("/kie.pdm").file)), File("build/gen/puml/mysql.sql"))
    }
}