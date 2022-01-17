package top.bettercode.generator.ddl

import top.bettercode.generator.GeneratorExtension
import top.bettercode.generator.database.entity.Table
import java.io.PrintWriter

object MysqlToDDL : ToDDL() {
    override val quoteMark: String = "`"
    override val commentPrefix: String = "#"

    override fun toDDLUpdate(
        oldTables: List<Table>,
        tables: List<Table>,
        out: PrintWriter,
        extension: GeneratorExtension
    ) {
        if (tables != oldTables) {
            val tableNames = tables.map { it.tableName }
            val oldTableNames = oldTables.map { it.tableName }
            if (extension.deleteTablesWhenUpdate)
                (oldTableNames - tableNames.toSet()).forEach {
                    out.println("$commentPrefix DROP $it")
                    out.println("DROP TABLE IF EXISTS $quote$it$quote;")
                    out.println()
                }
            val newTableNames = tableNames - oldTableNames.toSet()
            tables.forEach { table ->
                val tableName = table.tableName
                if (newTableNames.contains(tableName)) {
                    appendTable(table, out)
                } else {
                    val oldTable = oldTables.find { it.tableName == tableName }!!
                    if (oldTable != table) {
                        val lines = mutableListOf<String>()
                        if (oldTable.remarks != table.remarks)
                            lines.add("ALTER TABLE $quote$tableName$quote COMMENT '${table.remarks}';")

                        val oldColumns = oldTable.columns
                        val columns = table.columns
                        val oldPrimaryKeys = oldTable.primaryKeys
                        val primaryKeys = table.primaryKeys

                        if (oldPrimaryKeys.size == 1 && primaryKeys.size == 1) {
                            val oldPrimaryKey = oldPrimaryKeys[0]
                            val primaryKey = primaryKeys[0]
                            if (oldPrimaryKey != primaryKey) {
                                lines.add(
                                    "ALTER TABLE $quote$tableName$quote CHANGE $quote${oldPrimaryKey.columnName}$quote ${
                                        columnDef(
                                            primaryKey,
                                            quote
                                        )
                                    } COMMENT '${primaryKey.remarks}';"
                                )
                                oldColumns.remove(oldPrimaryKey)
                                columns.remove(primaryKey)
                            }
                        }

                        val oldColumnNames = oldColumns.map { it.columnName }
                        val columnNames = columns.map { it.columnName }
                        val dropColumnNames = oldColumnNames - columnNames.toSet()
                        if (extension.deleteTablesWhenUpdate)
                            dropColumnNames.forEach {
                                lines.add("ALTER TABLE $quote$tableName$quote DROP COLUMN $quote$it$quote;")
                            }
                        dropFk(oldColumns, dropColumnNames, lines, tableName)
                        val newColumnNames = columnNames - oldColumnNames.toSet()
                        columns.forEach { column ->
                            val columnName = column.columnName
                            if (newColumnNames.contains(columnName)) {
                                lines.add(
                                    "ALTER TABLE $quote$tableName$quote ADD COLUMN ${
                                        columnDef(
                                            column,
                                            quote
                                        )
                                    } COMMENT '${column.remarks}';"
                                )
                                addFk(column, lines, tableName, columnName)
                            } else {
                                val oldColumn = oldColumns.find { it.columnName == columnName }!!
                                if (column != oldColumn) {
                                    lines.add(
                                        "ALTER TABLE $quote$tableName$quote MODIFY ${
                                            columnDef(
                                                column,
                                                quote
                                            )
                                        } COMMENT '${column.remarks}';"
                                    )
                                    updateFk(column, oldColumn, lines, tableName)
                                }
                            }
                        }
                        if (extension.queryIndex)
                            updateIndexes(oldTable, table, lines, dropColumnNames)
                        if (lines.isNotEmpty()) {
                            out.println("$commentPrefix $tableName")
                            lines.forEach { out.println(it) }
                            out.println()
                        }
                    }
                }
            }

        }
    }

    override fun dropFkStatement(tableName: String, columnName: String): String =
        "ALTER TABLE $quote$tableName$quote DROP FOREIGN KEY ${
            foreignKeyName(
                tableName,
                columnName
            )
        };"

    override fun appendTable(table: Table, pw: PrintWriter) {
        val tableName = table.tableName
        pw.println("$commentPrefix $tableName")
        pw.println("DROP TABLE IF EXISTS $quote$tableName$quote;")
        pw.println("CREATE TABLE $quote$tableName$quote (")
        val hasPrimary = table.primaryKeyNames.isNotEmpty()
        val lastIndex = table.columns.size - 1
        table.columns.forEachIndexed { index, column ->
            pw.println(
                "  ${
                    columnDef(
                        column,
                        quote
                    )
                } COMMENT '${column.remarks}'${if (index < lastIndex || hasPrimary) "," else ""}"
            )
        }

        appendKeys(table, hasPrimary, pw, quote, tableName, useForeignKey)
        pw.println(")${if (table.physicalOptions.isNotBlank()) " ${table.physicalOptions}" else ""} COMMENT = '${table.remarks}';")

        appendIndexes(table, pw, quote)

        pw.println()
    }
}