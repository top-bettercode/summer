package top.bettercode.generator.ddl

import top.bettercode.generator.GeneratorExtension
import top.bettercode.generator.database.entity.Column
import top.bettercode.generator.database.entity.Table
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

    override fun dropFkStatement(tableName: String, columnName: String): String =
        "ALTER TABLE $quote$tableName$quote DROP FOREIGN KEY ${
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

    override fun appendTable(table: Table, pw: Writer) {
        val tableName = table.tableName
        pw.appendLine("$commentPrefix $tableName")
        pw.appendLine("DROP TABLE IF EXISTS $quote$tableName$quote;")
        pw.appendLine("CREATE TABLE $quote$tableName$quote ( -- '${table.remarks.replace("\\","\\\\")}'")
        val lastIndex = table.columns.size - 1
        table.columns.forEachIndexed { index, column ->
            pw.appendLine(
                "  ${
                    columnDef(
                        column,
                        quote
                    )
                } ${if (index < lastIndex) "," else ""} -- '${column.remarks.replace("\\","\\\\")}'"
            )
        }

        pw.appendLine(")${if (table.physicalOptions.isNotBlank()) " ${table.physicalOptions}" else ""};")

        appendIndexes(table, pw, quote)

        pw.appendLine()
    }
}