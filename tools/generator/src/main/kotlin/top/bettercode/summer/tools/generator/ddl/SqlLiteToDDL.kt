package top.bettercode.summer.tools.generator.ddl

import top.bettercode.summer.tools.generator.GeneratorExtension
import top.bettercode.summer.tools.generator.database.entity.Column
import top.bettercode.summer.tools.generator.database.entity.Table
import java.io.Writer

object SqlLiteToDDL : ToDDL() {
    override val quoteMark: String = "`"
    override val commentPrefix: String = "--"

    override fun toDDLUpdate(
            module: String,
            oldTables: List<Table>,
            tables: List<Table>,
            out: Writer,
            extension: GeneratorExtension
    ) {

    }

    override fun dropFkStatement(
            prefixTableName: String,
            tableName: String,
            columnName: String
    ): String =
            "ALTER TABLE $prefixTableName$quote$tableName$quote DROP FOREIGN KEY ${
                foreignKeyName(
                        tableName,
                        columnName
                )
            };"

    override fun columnDef(it: Column, quote: String): String {
        if (it.columnName.isBlank()) {
            throw Exception("${it.tableSchem ?: ""}:${it.columnName}:字段不能为空")
        }
        if (it.typeDesc.isBlank()) {
            throw Exception("${it.tableSchem ?: ""}:${it.columnName}:字段类型不能为空")
        }
        return "$quote${it.columnName}$quote ${it.typeDesc}${if (it.unsigned) " UNSIGNED" else ""}${it.defaultDesc}${if (it.extra.isNotBlank()) " ${it.extra}" else ""}${if (it.isPrimary) " PRIMARY KEY" else ""}${if (it.autoIncrement) " AUTOINCREMENT" else ""}${if (it.nullable) " NULL" else " NOT NULL"}"
    }

    override fun appendTable(prefixTableName: String, table: Table, pw: Writer) {
        val tableName = table.tableName
        pw.appendln("$commentPrefix $tableName")
        if (table.ext.dropTablesWhenUpdate)
            pw.appendln("DROP TABLE IF EXISTS $prefixTableName$quote$tableName$quote;")

        pw.appendln(
                "CREATE TABLE $prefixTableName$quote$tableName$quote ( -- '${
                    table.remarks.replace(
                            "\\",
                            "\\\\"
                    )
                }'"
        )

        val lastIndex = table.columns.size - 1
        table.columns.forEachIndexed { index, column ->
            pw.appendln(
                    "  ${
                        columnDef(
                                column,
                                quote
                        )
                    } ${if (index < lastIndex) "," else ""} -- '${
                        column.remarks.replace(
                                "\\",
                                "\\\\"
                        )
                    }'"
            )
        }

        pw.appendln(")${if (table.physicalOptions.isNotBlank()) " ${table.physicalOptions}" else ""};")

        appendIndexes(prefixTableName, table, pw, quote)

        pw.appendln()
    }
}