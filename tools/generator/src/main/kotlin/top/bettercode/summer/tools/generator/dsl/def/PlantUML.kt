package top.bettercode.summer.tools.generator.dsl.def

import top.bettercode.summer.tools.generator.database.entity.Column
import top.bettercode.summer.tools.generator.dom.unit.FileUnit
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
        destFile: File,
        private val remarksProperties: Properties?
) : Generator() {

    private val log = org.slf4j.LoggerFactory.getLogger(PlantUML::class.java)
    private val fklines = mutableListOf<String>()
    private val dest = FileUnit(destFile)

    override fun setUp() {
        dest.append(
                """@startuml ${if (umlModuleName.isNullOrBlank()) top.bettercode.summer.tools.generator.DataType.DATABASE.name else umlModuleName}

"""
        )
    }

    override fun call() {
        if (tableName.length > 30) {
            log.warn("表名：${tableName} 最好不要超过 30 个字符")
        }
        dest.append(
                """entity $tableName {
    $remarks
    ==
"""
        )

        table.pumlColumns.forEach {
            if (it is Column) {
                val isPrimary = it.isPrimary
                if (it.columnName.length > 30) {
                    log.warn("${table.tableName}表字段名：${it.columnName} 最好不要超过 30 个字符")
                }
                var prettyRemarks = it.prettyRemarks
                if (prettyRemarks.isBlank()) {
                    prettyRemarks = remarksProperties?.getProperty(it.columnName)?.trim() ?: ""
                }

                dest.append("    ${it.columnName} : ${it.typeDesc}${if (it.unsigned) " UNSIGNED" else ""}${if (isPrimary) " PK" else if (it.unique) " UNIQUE" else if (it.indexed) " INDEX" else ""}${if (it.autoIncrement) " AUTO_INCREMENT" else ""}${if (it.idgenerator.isBlank()) "" else " ${it.idgenerator}${if (it.idgeneratorParam.isBlank()) "" else "(${it.idgeneratorParam})"}"}${if (it.isPrimary && it.sequence.isNotBlank()) " SEQUENCE ${it.sequence}${if (it.sequenceStartWith != 1L) "(${it.sequenceStartWith})" else ""}" else ""}${it.defaultDesc}${if (it.nullable) " NULL" else " NOT NULL"}${if (it.extra.isNotBlank()) " ${it.extra}" else ""}${if (it.version) " VERSION" else ""}${if (it.createdDate) " CREATEDDATE" else ""}${if (it.createdBy) " CREATEDBY" else ""}${if (it.lastModifiedDate) " LASTMODIFIEDDATE" else ""}${if (it.lastModifiedBy) " LASTMODIFIEDBY" else ""}${if (it.logicalDelete) " LOGICALDELETE" else ""}${if (it.asBoolean) " ASBOOLEAN" else ""}${if (it.isForeignKey) " FK > ${it.pktableName}.${it.pkcolumnName}" else ""} -- $prettyRemarks\n")
                if (it.isForeignKey) {
                    fklines.add("${it.pktableName} ||--o{ $tableName")
                }
            } else {
                dest.append("    $it\n")
            }

        }
        table.indexes.filter { it.columnName.size > 1 }.forEach {
            dest.append(
                    "    '${if (it.unique) "UNIQUE" else "INDEX"} ${
                        it.columnName.joinToString(
                                ","
                        )
                    }\n"
            )
        }
        if (table.engine.isNotBlank() && !"InnoDB".equals(table.engine, true)) {
            dest.append("    'ENGINE = ${table.engine}\n")
        }
        dest.append("}\n\n")

    }

    override fun tearDown() {
        fklines.forEach {
            dest.append("$it\n")
        }
        if (fklines.isNotEmpty())
            dest.append("\n")

        dest.append(
                """
|@enduml
ENGINE
PK
FK
AUTO_INCREMENT
UNIQUE
INDEX
UNSIGNED
NOT
NULL
VERSION
CREATEDDATE
CREATEDBY
LASTMODIFIEDDATE
LASTMODIFIEDBY
LOGICALDELETE
ASBOOLEAN
DEFAULT
SEQUENCE
IDGENERATOR
""".trimMargin()
        )
        dest.writeTo()
    }

    fun appendLineText(text: String) {
        dest.append(text + "\n")
    }
}