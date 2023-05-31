package top.bettercode.summer.tools.generator.dsl.def

import top.bettercode.summer.tools.generator.database.entity.Column
import top.bettercode.summer.tools.generator.dsl.Generator
import java.io.File
import java.util.*

/**
 *
 * @author Peter Wu
 * @since 0.0.41
 */
class PlantUML(
        private val umlModuleName: String?,
        private val destFile: File,
        private val remarksProperties: Properties?
) : Generator() {

    private val fklines = mutableListOf<String>()

    override fun setUp() {
        destFile.parentFile.mkdirs()
        println(
                "${if (destFile.exists()) "覆盖" else "生成"}：${
                    destFile.absolutePath.substringAfter(
                            (ext.rootPath ?: ext.projectDir).absolutePath + File.separator
                    )
                }"
        )
        destFile.writeText(
                """PK
FK
UNIQUE
INDEX
IDGENERATOR
SEQUENCE
SOFTDELETE
ASBOOLEAN

@startuml ${if (umlModuleName.isNullOrBlank()) top.bettercode.summer.tools.generator.DataType.DATABASE.name else umlModuleName}

"""
        )
    }

    override fun call() {
        if (tableName.length > 32) {
            println("数据库对象的命名最好不要超过 32 个字符")
        }
        destFile.appendText(
                """entity $tableName {
    $remarks
    ==
"""
        )

        table.pumlColumns.forEach {
            if (it is Column) {
                val isPrimary = it.isPrimary
                if (it.columnName.length > 32) {
                    println("数据库对象的命名最好不要超过 32 个字符")
                }
                var prettyRemarks = it.prettyRemarks
                if (prettyRemarks.isBlank()) {
                    prettyRemarks = remarksProperties?.getProperty(it.columnName)?.trim() ?: ""
                }

                destFile.appendText("    ${it.columnName} : ${it.typeDesc}${if (it.unsigned) " UNSIGNED" else ""}${if (isPrimary) " PK" else if (it.unique) " UNIQUE" else if (it.indexed) " INDEX" else ""}${if (it.autoIncrement) " AUTO_INCREMENT" else ""}${if (it.idgenerator.isBlank()) "" else " ${it.idgenerator}"}${if (it.isPrimary && it.sequence.isNotBlank()) " SEQUENCE ${it.sequence}${if (it.sequenceStartWith != 1) " ${it.sequenceStartWith}" else ""}" else ""}${it.defaultDesc}${if (it.nullable) " NULL" else " NOT NULL"}${if (it.extra.isNotBlank()) " ${it.extra}" else ""}${if (it.version) " VERSION" else ""}${if (it.softDelete) " SOFTDELETE" else ""}${if (it.asBoolean) " ASBOOLEAN" else ""}${if (it.isForeignKey) " FK > ${it.pktableName}.${it.pkcolumnName}" else ""} -- $prettyRemarks\n")
                if (it.isForeignKey) {
                    fklines.add("${it.pktableName} ||--o{ $tableName")
                }
            } else {
                destFile.appendText("    $it\n")
            }

        }
        table.indexes.filter { it.columnName.size > 1 }.forEach {
            destFile.appendText(
                    "    '${if (it.unique) "UNIQUE" else "INDEX"} ${
                        it.columnName.joinToString(
                                ","
                        )
                    }\n"
            )
        }
        if (table.engine.isNotBlank() && !"InnoDB".equals(table.engine, true)) {
            destFile.appendText("    'ENGINE = ${table.engine}\n")
        }
        destFile.appendText("}\n\n")

    }

    override fun tearDown() {
        fklines.forEach {
            destFile.appendText("$it\n")
        }
        if (fklines.isNotEmpty())
            destFile.appendText("\n")

        destFile.appendText(
                """
                |@enduml
            """.trimMargin()
        )
    }

    fun appendLineText(text: String) {
        destFile.appendText(text + "\n")
    }
}