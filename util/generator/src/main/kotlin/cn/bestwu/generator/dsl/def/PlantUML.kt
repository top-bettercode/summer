package cn.bestwu.generator.dsl.def

import cn.bestwu.generator.DataType
import cn.bestwu.generator.database.domain.Column
import cn.bestwu.generator.dsl.Generator
import java.io.File

/**
 *
 * @author Peter Wu
 * @since 0.0.41
 */
class PlantUML(val myModuleName: String?, val output: String) : Generator() {

    override val destFile: File
        get() = File(output)

    override fun setUp() {
        destFile.parentFile.mkdirs()
        destFile.writeText("""PK
FK
UNIQUE
INDEX

@startuml ${if (myModuleName.isNullOrBlank()) DataType.DATABASE.name else myModuleName}

""")
    }

    override fun doCall() {
        destFile.appendText("""class ${if (catalog.isNullOrBlank()) "" else "$catalog."}$tableName ${table.desc} {
    $remarks
    ==
""")
        val fklines = mutableListOf<String>()
        table.pumlColumns.forEach {
            if (it is Column) {
                val isPrimary = it.isPrimary
                destFile.appendText("    ${it.columnName} : ${it.typeDesc}${if (isPrimary) " PK" else if (it.unique) " UNIQUE" else if (it.indexed) " INDEX" else ""}${it.defaultDesc}${if (it.extra.isNotBlank()) " ${it.extra}" else ""}${if (it.autoIncrement) " AUTO_INCREMENT" else ""}${if (it.nullable) " NULL" else " NOT NULL"}${if (it.isForeignKey) " FK > ${it.pktableName}.${it.pkcolumnName}" else ""} -- ${it.remarks}\n")
                if (it.isForeignKey) {
                    fklines.add("${it.pktableName} \"1\" -- \"0..*\" $tableName")
                }
            } else {
                destFile.appendText("    $it\n")
            }

        }
        indexes.filter { it.columnName.size > 1 }.forEach {
            destFile.appendText("    '${if (it.unique) "UNIQUE" else "INDEX"} ${it.columnName.joinToString(",")}\n")
        }
        destFile.appendText("}\n\n")
        fklines.forEach {
            destFile.appendText("$it\n")
        }
        if (fklines.isNotEmpty())
            destFile.appendText("\n")
    }

    override fun tearDown() {
        destFile.appendText("""
                |@enduml
            """.trimMargin())
    }

    fun appendlnText(text: String) {
        destFile.appendText(text + "\n\n")
    }
}