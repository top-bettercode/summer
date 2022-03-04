package top.bettercode.generator.puml

import org.junit.jupiter.api.Test
import top.bettercode.generator.GeneratorExtension
import top.bettercode.generator.GeneratorExtension.Companion.defaultModuleName
import top.bettercode.generator.ddl.MysqlToDDL
import top.bettercode.generator.ddl.OracleToDDL
import top.bettercode.generator.dom.unit.FileUnit
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
    val newOraclePuml = File(
        PumlConverterTest::class.java.getResource("/puml/src/neworacle.puml")?.file
            ?: throw IllegalStateException()
    )
    val mysqlPuml = File(
        PumlConverterTest::class.java.getResource("/puml/src/mysql.puml")?.file
            ?: throw IllegalStateException()
    )
    val newMysqlPuml = File(
        PumlConverterTest::class.java.getResource("/puml/src/newmysql.puml")?.file
            ?: throw IllegalStateException()
    )

    @Test
    fun convert() {
        val tables = PumlConverter.toTables(oraclePuml, defaultModuleName)
        println(tables)
    }

    @Test
    fun compile() {
        PumlConverter.compile(
            GeneratorExtension(),
            defaultModuleName,
            oraclePuml,
            File("build/gen/puml/database.puml")
        )
    }

    @Test
    fun toMysql() {
        PumlConverter.toMysql(
            GeneratorExtension(),
            defaultModuleName,
            oraclePuml,
            File("build/gen/puml/database.puml")
        )
    }

    @Test
    fun toOracle() {
        PumlConverter.toOracle(
            GeneratorExtension(),
            defaultModuleName,
            mysqlPuml,
            File("build/gen/puml/database.puml")
        )
    }

    @Test
    fun toOracleDLL() {
        val out = FileUnit("build/gen/puml/oracle.sql")
        OracleToDDL.toDDL(
            PumlConverter.toTables(oraclePuml, defaultModuleName),
            out
        )
        out.writeTo()
    }

    @Test
    fun toMySqlDLL() {
        val out = FileUnit("build/gen/puml/mysql.sql")
        MysqlToDDL.toDDL(
            PumlConverter.toTables(oraclePuml, defaultModuleName),
            out
        )
        out.writeTo()
    }

    @Test
    fun toOracleUpdate() {
        File("build/gen/puml/oracleUpdate.sql").printWriter().use {
            OracleToDDL.toDDLUpdate(
                defaultModuleName,
                PumlConverter.toTables(oraclePuml, defaultModuleName),
                PumlConverter.toTables(newOraclePuml, defaultModuleName),
                it,
                GeneratorExtension()
            )
        }
    }

    @Test
    fun toMysqlUpdate() {
        File("build/gen/puml/mysqlUpdate.sql").printWriter().use {
            MysqlToDDL.toDDLUpdate(
                defaultModuleName,
                PumlConverter.toTables(
                    mysqlPuml, defaultModuleName
                ),
                PumlConverter.toTables(newMysqlPuml, defaultModuleName),
                it,
                GeneratorExtension()
            )
        }
    }
}