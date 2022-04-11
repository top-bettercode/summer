package top.bettercode.generator.ddl

import top.bettercode.generator.GeneratorExtension
import top.bettercode.generator.database.entity.Table
import java.io.Writer

object MysqlToDDL : ToDDL() {
    override val quoteMark: String = "`"
    override val commentPrefix: String = "#"

    override fun toDDLUpdate(
        module: String,
        oldTables: List<Table>,
        tables: List<Table>,
        out: Writer,
        extension: GeneratorExtension
    ) {
        if (tables != oldTables) {
            val tableNames = tables.map { it.tableName }
            val oldTableNames = oldTables.map { it.tableName }
            if (extension.dropTablesWhenUpdate)
                (oldTableNames - tableNames.toSet()).forEach {
                    out.appendLine("$commentPrefix DROP $it")
                    out.appendLine("DROP TABLE IF EXISTS $quote$it$quote;")
                    out.appendLine()
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
                        if (extension.dropColumnsWhenUpdate)
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
                        if (extension.datasources[module]?.queryIndex == true)
                            updateIndexes(oldTable, table, lines, dropColumnNames)
                        if (lines.isNotEmpty()) {
                            out.appendLine("$commentPrefix $tableName")
                            lines.forEach { out.appendLine(it) }
                            out.appendLine()
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

    override fun appendTable(table: Table, pw: Writer) {
        val tableName = table.tableName
        pw.appendLine("$commentPrefix $tableName")
        if (table.ext.dropTablesWhenUpdate)
            pw.appendLine("DROP TABLE IF EXISTS $quote$tableName$quote;")
        pw.appendLine("CREATE TABLE $quote$tableName$quote (")
        val hasPrimary = table.primaryKeyNames.isNotEmpty()
        val lastIndex = table.columns.size - 1
        table.columns.forEachIndexed { index, column ->
            pw.appendLine(
                "  ${
                    columnDef(
                        column,
                        quote
                    )
                } COMMENT '${column.remarks}'${if (index < lastIndex || hasPrimary) "," else ""}"
            )
        }

        appendKeys(table, hasPrimary, pw, quote, tableName, useForeignKey)
        pw.appendLine(")${if (table.physicalOptions.isNotBlank()) " ${table.physicalOptions}" else ""} COMMENT = '${table.remarks}';")

        appendIndexes(table, pw, quote)

        pw.appendLine()
    }
}