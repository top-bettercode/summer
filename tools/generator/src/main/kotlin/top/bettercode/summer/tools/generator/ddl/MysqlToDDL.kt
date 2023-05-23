package top.bettercode.summer.tools.generator.ddl

import top.bettercode.summer.tools.generator.GeneratorExtension
import top.bettercode.summer.tools.generator.database.entity.Table
import java.io.Writer

object MysqlToDDL : ToDDL() {
    override val quoteMark: String = "`"
    override val commentPrefix: String = "#"

    override fun toDDLUpdate(module: String, oldTables: List<Table>, tables: List<Table>, out: Writer, extension: GeneratorExtension) {
        out.appendln("$commentPrefix ${extension.datasource(module).url.substringBefore("?")}")
        out.appendln("$commentPrefix use ${extension.datasource(module).schema};")
        out.appendln()
        if (tables != oldTables) {
            val schema = if (extension.enable("include-schema")) {
                "$quote${extension.datasource(module).schema}$quote."
            } else {
                ""
            }
            val tableNames = tables.map { it.tableName }
            val oldTableNames = oldTables.map { it.tableName }
            if (extension.dropTablesWhenUpdate) (oldTableNames - tableNames.toSet()).filter { "api_token" != it }.forEach {
                out.appendln("$commentPrefix DROP $it")
                out.appendln("DROP TABLE IF EXISTS $schema$quote$it$quote;")
                out.appendln()
            }
            val newTableNames = tableNames - oldTableNames.toSet()
            tables.forEach { table ->
                val tableName = table.tableName
                if (newTableNames.contains(tableName)) {
                    appendTable(schema, table, out)
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
                        if (extension.dropColumnsWhenUpdate) dropColumnNames.forEach {
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


                        if (extension.datasources[module]?.queryIndex == true) updateIndexes(schema, oldTable, table, lines, dropColumnNames)

                        if (!oldTable.engine.equals(table.engine, true)) lines.add("ALTER TABLE $schema$quote$tableName$quote ENGINE ${table.engine};")

                        //out change
                        if (lines.isNotEmpty()) {
                            out.appendln("$commentPrefix $tableName")
                            lines.forEach { out.appendln(it) }
                            out.appendln()
                        }

                    }
                }
            }

        }
    }

    override fun dropFkStatement(prefixTableName: String, tableName: String, columnName: String): String = "ALTER TABLE $prefixTableName$quote$tableName$quote DROP FOREIGN KEY ${
        foreignKeyName(tableName, columnName)
    };"

    override fun appendTable(prefixTableName: String, table: Table, pw: Writer) {
        val tableName = table.tableName
        pw.appendln("$commentPrefix $tableName")
        if (table.ext.dropTablesWhenUpdate) pw.appendln("DROP TABLE IF EXISTS $prefixTableName$quote$tableName$quote;")
        pw.appendln("CREATE TABLE $prefixTableName$quote$tableName$quote (")
        val hasPrimary = table.primaryKeyNames.isNotEmpty()
        val lastIndex = table.columns.size - 1
        table.columns.forEachIndexed { index, column ->
            pw.appendln("  ${
                columnDef(column, quote)
            } COMMENT '${
                column.remarks.replace("\\", "\\\\")
            }'${if (index < lastIndex || hasPrimary) "," else ""}")
        }

        appendKeys(table, hasPrimary, pw, quote, tableName, useForeignKey)
        pw.appendln(") DEFAULT CHARSET = utf8mb4${if (table.physicalOptions.isNotBlank()) " ${table.physicalOptions}" else ""} COMMENT = '${
            table.remarks.replace("\\", "\\\\")
        }'${if (table.engine.isNotBlank()) " ENGINE = ${table.engine};" else ""};")

        appendIndexes(prefixTableName, table, pw, quote)

        pw.appendln()
    }
}