package top.bettercode.generator.dsl.def

import top.bettercode.generator.DataType
import top.bettercode.generator.database.entity.Column
import top.bettercode.generator.dsl.Generator
import java.io.File
import java.util.*

/**
 *
 * @author Peter Wu
 * @since 0.0.41
 */
class PlantUML(
    private val myModuleName: String?,
    private val output: String,
    private val remarksProperties: Properties?
) : Generator() {

    private val fklines = mutableListOf<String>()
    override val destFile: File
        get() = File(output)

    override fun setUp() {
        destFile.parentFile.mkdirs()
        println(
            "${if (destFile.exists()) "覆盖" else "生成"}：${
                destFile.absolutePath.substringAfter(
                    (extension.rootPath ?: basePath).absolutePath + File.separator
                )
            }"
        )
        destFile.writeText(
            """PK
FK
UNIQUE
INDEX

@startuml ${if (myModuleName.isNullOrBlank()) DataType.DATABASE.name else myModuleName}

"""
        )
    }

    override fun doCall() {
        if (tableName.length > 32) {
            println("数据库对象的命名最好不要超过 32 个字符")
        }
        destFile.appendText(
            """entity ${if (catalog.isNullOrBlank()) "" else "$catalog."}${tableName} {
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

                destFile.appendText("    ${it.columnName} : ${it.typeDesc}${if (it.unsigned) " UNSIGNED" else ""}${if (isPrimary) " PK" else if (it.unique) " UNIQUE" else if (it.indexed) " INDEX" else ""}${it.defaultDesc}${if (it.extra.isNotBlank()) " ${it.extra}" else ""}${if (it.autoIncrement) " AUTO_INCREMENT" else ""}${if (it.idgenerator) " IDGENERATOR" else ""}${if (it.isPrimary && table.sequence.isNotBlank()) " SEQUENCE ${table.sequence}${if (table.sequenceStartWith != 1) " ${table.sequenceStartWith}" else ""}" else ""}${if (it.nullable) " NULL" else " NOT NULL"}${if (it.isForeignKey) " FK > ${it.pktableName}.${it.pkcolumnName}" else ""} -- $prettyRemarks\n")
                if (it.isForeignKey) {
                    fklines.add("${it.pktableName} ||--o{ $tableName")
                }
            } else {
                destFile.appendText("    $it\n")
            }

        }
        indexes.filter { it.columnName.size > 1 }.forEach {
            destFile.appendText(
                "    '${if (it.unique) "UNIQUE" else "INDEX"} ${
                    it.columnName.joinToString(
                        ","
                    )
                }\n"
            )
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

    fun appendlnText(text: String) {
        destFile.appendText(text + "\n\n")
    }
}