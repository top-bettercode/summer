package cn.bestwu.generator.ddl

import cn.bestwu.generator.database.entity.Table
import java.io.PrintWriter

object SqlLiteToDDL : ToDDL() {
    override val quoteMark: String = "`"
    override val commentPrefix: String = "--"

    override fun toDDLUpdate(oldTables: List<Table>, tables: List<Table>, out: PrintWriter, deleteTablesWhenUpdate: Boolean) {

    }

    override fun dropFkStatement(tableName: String, columnName: String): String = "ALTER TABLE $quote$tableName$quote DROP FOREIGN KEY ${foreignKeyName(tableName, columnName)};"

    override fun appendTable(table: Table, pw: PrintWriter) {
        val tableName = table.tableName
        pw.println("$commentPrefix $tableName")
        pw.println("DROP TABLE IF EXISTS $quote$tableName$quote;")
        pw.println("CREATE TABLE $quote$tableName$quote ( -- '${table.remarks}'")
        val hasPrimary = table.primaryKeyNames.isNotEmpty()
        val lastIndex = table.columns.size - 1
        table.columns.forEachIndexed { index, column ->
            pw.println("  ${columnDef(column, quote)} ${if (index < lastIndex || hasPrimary) "," else ""} -- '${column.remarks}'")
        }

        appendKeys(table, hasPrimary, pw, quote, tableName, useForeignKey)
        pw.println(")${if (table.physicalOptions.isNotBlank()) " ${table.physicalOptions}" else ""};")

        appendIndexes(table, pw, quote)

        pw.println()
    }
}