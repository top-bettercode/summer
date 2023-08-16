package top.bettercode.summer.tools.generator.ddl

import top.bettercode.summer.tools.generator.JDBCConnectionConfiguration
import top.bettercode.summer.tools.generator.database.entity.Table
import java.io.Writer

object MysqlToDDL : ToDDL() {
    override val quoteMark: String = "`"
    override val commentPrefix: String = "#"

    override fun toDDLUpdate(oldTables: List<Table>, tables: List<Table>, out: Writer, databaseConf: JDBCConnectionConfiguration) {
        out.appendLine("$commentPrefix ${databaseConf.url.substringBefore("?")}")
        out.appendLine("$commentPrefix use ${databaseConf.schema};")
        out.appendLine()
        if (tables != oldTables) {
            val schema = if (databaseConf.includeSchema) {
                "$quote${databaseConf.schema}$quote."
            } else {
                ""
            }
            val tableNames = tables.map { it.tableName }
            val oldTableNames = oldTables.map { it.tableName }
            if (databaseConf.dropTablesWhenUpdate) (oldTableNames - tableNames.toSet()).filter { "api_token" != it }.forEach {
                out.appendLine("$commentPrefix DROP $it")
                out.appendLine("DROP TABLE IF EXISTS $schema$quote$it$quote;")
                out.appendLine()
            }
            val newTableNames = tableNames - oldTableNames.toSet()
            tables.forEach { table ->
                val tableName = table.tableName
                if (newTableNames.contains(tableName)) {
                    appendTable(schema, table, out, databaseConf)
                } else {
                    val oldTable = oldTables.find { it.tableName == tableName }!!
                    if (oldTable != table) {
                        val lines = mutableListOf<String>()
                        if (oldTable.remarks != table.remarks) lines.add("ALTER TABLE $schema$quote$tableName$quote COMMENT '${
                            table.remarks.replace("\\", "\\\\")
                        }';")


                        val oldColumns = oldTable.columns
                        val columns = table.columns
                        val oldPrimaryKeys = oldTable.primaryKeys.toMutableSet()
                        val primaryKeys = table.primaryKeys


                        val oldColumnNames = oldColumns.map { it.columnName }
                        val columnNames = columns.map { it.columnName }
                        val dropColumnNames = oldColumnNames - columnNames.toSet()
                        if (databaseConf.dropColumnsWhenUpdate) dropColumnNames.forEach {
                            lines.add("ALTER TABLE $schema$quote$tableName$quote DROP COLUMN $quote$it$quote;")
                            oldPrimaryKeys.removeIf { pk -> pk.columnName == it }
                        }
                        dropFk(schema, oldColumns, dropColumnNames, lines, tableName)
                        val newColumnNames = columnNames - oldColumnNames.toSet()
                        columns.forEach { column ->
                            val columnName = column.columnName
                            if (newColumnNames.contains(columnName)) {
                                lines.add("ALTER TABLE $schema$quote$tableName$quote ADD COLUMN ${
                                    columnDef(column, quote)
                                } COMMENT '${column.remarks.replace("\\", "\\\\")}';")
                                addFk(schema, column, lines, tableName, columnName)
                            } else {
                                val oldColumn = oldColumns.find { it.columnName == columnName }!!
                                if (column != oldColumn) {
                                    lines.add("ALTER TABLE $schema$quote$tableName$quote MODIFY ${
                                        columnDef(column, quote)
                                    } COMMENT '${column.remarks.replace("\\", "\\\\")}';")
                                    updateFk(schema, column, oldColumn, lines, tableName)
                                }
                            }
                        }

                        if (oldPrimaryKeys.size == 1 && primaryKeys.size == 1) {
                            val oldPrimaryKey = oldPrimaryKeys.first()
                            val primaryKey = primaryKeys[0]
                            if (oldPrimaryKey != primaryKey) {
                                lines.add("ALTER TABLE $schema$quote$tableName$quote CHANGE $quote${oldPrimaryKey.columnName}$quote ${
                                    columnDef(primaryKey, quote)
                                } COMMENT '${primaryKey.remarks.replace("\\", "\\\\")}';")
                                oldColumns.remove(oldPrimaryKey)
                                columns.remove(primaryKey)
                            }
                        } else if (oldPrimaryKeys != primaryKeys) {
                            if (oldPrimaryKeys.isNotEmpty())
                                lines.add("ALTER TABLE $schema$quote$tableName$quote DROP PRIMARY KEY;")
                            if (table.primaryKeyNames.isNotEmpty()) {
                                lines.add("ALTER TABLE $schema$quote$tableName$quote ADD PRIMARY KEY(${table.primaryKeyNames.joinToString(",") { "$quote$it$quote" }});")
                            }
                        }


                        if (databaseConf.queryIndex) updateIndexes(schema, oldTable, table, lines, dropColumnNames)

                        if (!oldTable.engine.equals(table.engine, true)) lines.add("ALTER TABLE $schema$quote$tableName$quote ENGINE ${table.engine};")

                        //out change
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

    override fun dropFkStatement(prefixTableName: String, tableName: String, columnName: String): String = "ALTER TABLE $prefixTableName$quote$tableName$quote DROP FOREIGN KEY ${
        foreignKeyName(tableName, columnName)
    };"

    override fun appendTable(prefixTableName: String, table: Table, pw: Writer, databaseConf: JDBCConnectionConfiguration) {
        val tableName = table.tableName
        pw.appendLine("$commentPrefix $tableName")
        if (databaseConf.dropTablesWhenUpdate) pw.appendLine("DROP TABLE IF EXISTS $prefixTableName$quote$tableName$quote;")
        pw.appendLine("CREATE TABLE $prefixTableName$quote$tableName$quote (")
        val hasPrimary = table.primaryKeyNames.isNotEmpty()
        val lastIndex = table.columns.size - 1
        table.columns.forEachIndexed { index, column ->
            pw.appendLine("  ${
                columnDef(column, quote)
            } COMMENT '${
                column.remarks.replace("\\", "\\\\")
            }'${if (index < lastIndex || hasPrimary) "," else ""}")
        }

        appendKeys(table, hasPrimary, pw, quote, tableName, useForeignKey)
        pw.appendLine(") DEFAULT CHARSET = ${databaseConf.charset} COLLATE ${databaseConf.collate} ${if (table.physicalOptions.isNotBlank()) " ${table.physicalOptions}" else ""} COMMENT = '${
            table.remarks.replace("\\", "\\\\")
        }'${if (table.engine.isNotBlank()) " ENGINE = ${table.engine};" else ""};")

        appendIndexes(prefixTableName, table, pw, quote)

        pw.appendLine()
    }
}