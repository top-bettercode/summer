package cn.bestwu.generator.ddl

import cn.bestwu.generator.database.domain.Table
import java.io.PrintWriter

object MysqlToDDL : ToDDL() {
    override val quoteMark: String = "`"
    override val commentPrefix: String = "#"

    override fun toDDLUpdate(oldTables: List<Table>, tables: List<Table>, out: PrintWriter, deleteTablesWhenUpdate: Boolean) {
        if (tables != oldTables) {
            val tableNames = tables.map { it.tableName }
            val oldTableNames = oldTables.map { it.tableName }
            if (deleteTablesWhenUpdate)
                (oldTableNames - tableNames).forEach {
                    out.println("$commentPrefix DROP $it")
                    out.println("DROP TABLE IF EXISTS $quote$it$quote;")
                    out.println()
                }
            val newTableNames = tableNames - oldTableNames
            tables.forEach { table ->
                val tableName = table.tableName
                if (newTableNames.contains(tableName)) {
                    appendTable(table, out)
                } else {
                    val oldTable = oldTables.find { it.tableName == tableName }!!
                    if (oldTable != table) {
                        out.println("$commentPrefix $tableName")
                        if (oldTable.remarks.trimEnd('表') != table.remarks)
                            out.println("ALTER TABLE $quote$tableName$quote COMMENT '${table.remarks}';")

                        val oldColumns = oldTable.columns
                        val columns = table.columns
                        val oldPrimaryKeys = oldTable.primaryKeys
                        val primaryKeys = table.primaryKeys
                        val oldPrimaryKey = oldPrimaryKeys[0]
                        val primaryKey = primaryKeys[0]
                        if (oldPrimaryKeys.size == 1 && primaryKeys.size == 1 && oldPrimaryKey != primaryKey) {
                            out.println("ALTER TABLE $quote$tableName$quote CHANGE $quote${oldPrimaryKey.columnName}$quote ${columnDef(primaryKey, quote)} COMMENT '${primaryKey.remarks}';")
                            oldColumns.remove(oldPrimaryKey)
                            columns.remove(primaryKey)
                        }
                        val oldColumnNames = oldColumns.map { it.columnName }
                        val columnNames = columns.map { it.columnName }
                        val dropColumnNames = oldColumnNames - columnNames
                        dropColumnNames.forEach {
                            out.println("ALTER TABLE $quote$tableName$quote DROP COLUMN $quote$it$quote;")
                        }
                        dropFk(oldColumns, dropColumnNames, out, tableName)
                        val newColumnNames = columnNames - oldColumnNames
                        columns.forEach { column ->
                            val columnName = column.columnName
                            if (newColumnNames.contains(columnName)) {
                                out.println("ALTER TABLE $quote$tableName$quote ADD COLUMN ${columnDef(column, quote)} COMMENT '${column.remarks}';")
                                addFk(column, out, tableName, columnName)
                            } else {
                                val oldColumn = oldColumns.find { it.columnName == columnName }!!
                                if (column != oldColumn) {
                                    out.println("ALTER TABLE $quote$tableName$quote MODIFY ${columnDef(column, quote)} COMMENT '${column.remarks}';")
                                    updateFk(column, oldColumn, out, tableName)
                                }
                            }
                        }
                        updateIndexes(oldTable, table, out)

                        out.println()
                    }
                }
            }

        }
    }

    override fun dropFkStatement(tableName: String, columnName: String): String = "ALTER TABLE $quote$tableName$quote DROP FOREIGN KEY ${foreignKeyName(tableName, columnName)};"

    override fun appendTable(table: Table, pw: PrintWriter) {
        val tableName = table.tableName
        pw.println("$commentPrefix $tableName")
        pw.println("DROP TABLE IF EXISTS $quote$tableName$quote;")
        pw.println("CREATE TABLE $quote$tableName$quote (")
        val hasPrimary = table.primaryKeyNames.isNotEmpty()
        val lastIndex = table.columns.size - 1
        table.columns.forEachIndexed { index, column ->
            pw.println("  ${columnDef(column, quote)} COMMENT '${column.remarks}'${if (index < lastIndex || hasPrimary) "," else ""}")
        }

        appendKeys(table, hasPrimary, pw, quote, tableName, useForeignKey)
        pw.println(")${if (table.physicalOptions.isNotBlank()) " ${table.physicalOptions}" else ""} COMMENT = '${table.remarks}';")

        appendIndexes(table, pw, quote)

        pw.println()
    }
}