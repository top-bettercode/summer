package top.bettercode.summer.tools.generator.ddl

import top.bettercode.summer.tools.generator.DatabaseConfiguration
import top.bettercode.summer.tools.generator.database.entity.Table
import java.io.Writer

object MysqlToDDL : ToDDL() {
    override val quoteMark: String = "`"
    override val commentPrefix: String = "#"

    override fun toDDLUpdate(oldTables: List<Table>, tables: List<Table>, out: Writer, database: DatabaseConfiguration) {
        out.appendLine("$commentPrefix ${database.url.substringBefore("?")}")
        out.appendLine("$commentPrefix use ${database.schema};")
        out.appendLine()
        val notOnlyCollate = !database.extension.setting("onlyCollate", "false").toBoolean()
        val notOnlyComment = !database.extension.setting("onlyComment", "false").toBoolean()
        val noComment = database.extension.setting("noComment", "false").toBoolean()
        if (!database.offline && notOnlyComment) {
            //schema default collate change
            database.use {
                if (database.collate != null && getSchemaDefaultCollate() != database.collate) {
                    out.appendLine("$commentPrefix schema default collate change")
                    //alter database testffjp character set utf8mb4 collate utf8mb4_unicode_ci;
                    out.appendLine("ALTER DATABASE ${database.schema} CHARACTER SET ${database.collate!!.substringBefore("_")} COLLATE ${database.collate};")
                    out.appendLine()
                }
            }
        }
        if (tables != oldTables) {
            val schema = if (database.includeSchema) {
                "$quote${database.schema}$quote."
            } else {
                ""
            }
            val tableNames = tables.map { it.tableName }
            val oldTableNames = oldTables.map { it.tableName }
            if (notOnlyCollate && notOnlyComment && database.dropTablesWhenUpdate) (oldTableNames - tableNames.toSet()).filter { "api_token" != it }.forEach {
                out.appendLine("$commentPrefix DROP $it")
                out.appendLine("DROP TABLE IF EXISTS $schema$quote$it$quote;")
                out.appendLine()
            }
            val newTableNames = tableNames - oldTableNames.toSet()
            tables.forEach { table ->
                val tableName = table.tableName
                if (newTableNames.contains(tableName)) {
                    if (notOnlyCollate && notOnlyComment)
                        appendTable(schema, table, out, database)
                } else {
                    val oldTable = oldTables.find { it.tableName == tableName }!!
                    if (oldTable != table) {
                        val lines = mutableListOf<String>()
                        if (notOnlyCollate) {
                            if (oldTable.remarks != table.remarks) lines.add("ALTER TABLE $schema$quote$tableName$quote COMMENT '${
                                table.remarks.replace("\\", "\\\\")
                            }';")

                            val oldColumns = oldTable.columns
                            val columns = table.columns
                            val oldPrimaryKeys = oldTable.primaryKeys.toMutableSet()
                            val primaryKeys = table.primaryKeys.toSet()


                            val oldColumnNames = oldColumns.map { it.columnName }
                            val columnNames = columns.map { it.columnName }
                            val dropColumnNames = oldColumnNames - columnNames.toSet()
                            if (notOnlyComment) {
                                if (database.dropColumnsWhenUpdate) dropColumnNames.forEach {
                                    lines.add("ALTER TABLE $schema$quote$tableName$quote DROP COLUMN $quote$it$quote;")
                                    oldPrimaryKeys.removeIf { pk -> pk.columnName == it }
                                }
                                dropFk(schema, oldColumns, dropColumnNames, lines, tableName)
                            }
                            val newColumnNames = columnNames - oldColumnNames.toSet()
                            columns.forEach { column ->
                                val columnName = column.columnName
                                if (newColumnNames.contains(columnName)) {
                                    if (notOnlyComment) {
                                        lines.add("ALTER TABLE $schema$quote$tableName$quote ADD COLUMN ${
                                            columnDef(column, quote)
                                        } COMMENT '${column.remarks.replace("\\", "\\\\")}';")
                                        addFk(schema, column, lines, tableName, columnName)
                                    }
                                } else {
                                    val oldColumn = oldColumns.find { it.columnName == columnName }!!
                                    if (column != oldColumn) {
                                        if (notOnlyComment) {
                                            if (noComment) {
                                                val columnDef = columnDef(column, quote)
                                                val oldColumnDef = columnDef(oldColumn, quote)
                                                if (columnDef != oldColumnDef) {
                                                    lines.add("ALTER TABLE $schema$quote$tableName$quote MODIFY ${
                                                        columnDef(column, quote)
                                                    } COMMENT '${column.remarks.replace("\\", "\\\\")}';")
                                                }
                                            } else {
                                                lines.add("ALTER TABLE $schema$quote$tableName$quote MODIFY ${
                                                    columnDef(column, quote)
                                                } COMMENT '${column.remarks.replace("\\", "\\\\")}';")
                                            }
                                            updateFk(schema, column, oldColumn, lines, tableName)
                                        } else if (oldColumn.remarks != column.remarks) {
                                            lines.add("ALTER TABLE $schema$quote$tableName$quote MODIFY ${
                                                columnDef(oldColumn, quote)
                                            } COMMENT '${column.remarks.replace("\\", "\\\\")}';")
                                        }
                                    }
                                }
                            }

                            if (notOnlyComment) {
                                if (oldPrimaryKeys.size == 1 && primaryKeys.size == 1) {
                                    val oldPrimaryKey = oldPrimaryKeys.first()
                                    val primaryKey = primaryKeys.first()
                                    if (oldPrimaryKey != primaryKey) {
                                        lines.add("ALTER TABLE $schema$quote$tableName$quote CHANGE $quote${oldPrimaryKey.columnName}$quote ${
                                            columnDef(primaryKey, quote)
                                        } COMMENT '${primaryKey.remarks.replace("\\", "\\\\")}';")
                                    }
                                } else if (oldPrimaryKeys != primaryKeys) {
                                    if (oldPrimaryKeys.isNotEmpty())
                                        lines.add("ALTER TABLE $schema$quote$tableName$quote DROP PRIMARY KEY;")
                                    if (table.primaryKeyNames.isNotEmpty()) {
                                        lines.add("ALTER TABLE $schema$quote$tableName$quote ADD PRIMARY KEY(${table.primaryKeyNames.joinToString(",") { "$quote$it$quote" }});")
                                    }
                                }

                                if (database.queryIndex) updateIndexes(schema, oldTable, table, lines, dropColumnNames)

                                //engine change
                                if (!oldTable.engine.equals(table.engine, true)) lines.add("ALTER TABLE $schema$quote$tableName$quote ENGINE ${table.engine};")
                            }
                        }
                        if (notOnlyComment) {
                            //charset change
                            if (oldTable.collate != null && table.collate != null && (oldTable.charset != table.charset || oldTable.collate != table.collate)) {
                                lines.add("ALTER TABLE $schema$quote$tableName$quote CONVERT TO CHARACTER SET ${table.charset} COLLATE ${table.collate};")
                            }
                        }
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

    override fun appendTable(prefixTableName: String, table: Table, pw: Writer, database: DatabaseConfiguration) {
        val tableName = table.tableName
        pw.appendLine("$commentPrefix $tableName")
        if (database.dropTablesWhenUpdate) pw.appendLine("DROP TABLE IF EXISTS $prefixTableName$quote$tableName$quote;")
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
        pw.appendLine(")${if (table.collate.isNullOrBlank()) "" else " DEFAULT CHARSET = ${table.charset} COLLATE ${table.collate}"}${if (table.physicalOptions.isNotBlank()) " ${table.physicalOptions}" else ""} COMMENT = '${
            table.remarks.replace("\\", "\\\\")
        }'${if (table.engine.isNotBlank()) " ENGINE = ${table.engine}" else ""};")

        appendIndexes(prefixTableName, table, pw, quote)

        pw.appendLine()
    }
}