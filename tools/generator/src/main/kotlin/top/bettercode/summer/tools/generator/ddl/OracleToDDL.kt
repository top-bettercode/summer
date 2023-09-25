package top.bettercode.summer.tools.generator.ddl

import top.bettercode.summer.tools.generator.DatabaseConfiguration
import top.bettercode.summer.tools.generator.database.entity.Column
import top.bettercode.summer.tools.generator.database.entity.Table
import java.io.Writer

object OracleToDDL : ToDDL() {
    override val quoteMark: String = "\""
    override val commentPrefix: String = "--"

    override fun toDDLUpdate(
            oldTables: List<Table>,
            tables: List<Table>,
            out: Writer,
            database: DatabaseConfiguration
    ) {
        out.appendLine("$commentPrefix ${database.url.substringBefore("?")} schema:${database.schema}")
        out.appendLine()
        if (tables != oldTables) {
            val schema = if (database.includeSchema) {
                "$quote${database.schema}$quote."
            } else {
                ""
            }
            val tableNames = tables.map { it.tableName }
            val oldTableNames = oldTables.map { it.tableName }
            if (database.dropTablesWhenUpdate)
                (oldTableNames - tableNames.toSet()).filter { "api_token" != it }
                        .forEach { tableName ->
                            out.appendLine("$commentPrefix DROP $schema$tableName")
                            val primaryKey = oldTables.find { it.tableName == tableName }!!.primaryKey
                            if (primaryKey?.sequence?.isNotBlank() == true)
                                out.appendLine("DROP SEQUENCE $schema$quote${primaryKey.sequence}$quote;")
                            out.appendLine("DROP TABLE $schema$quote$tableName$quote;")
                            out.appendLine()
                        }
            val newTableNames = tableNames - oldTableNames.toSet()
            tables.forEach { table ->
                val tableName = table.tableName
                if (newTableNames.contains(tableName)) {
                    appendTable(prefixTableName = schema, table = table, pw = out, database = database)
                } else {
                    val oldTable = oldTables.find { it.tableName == tableName }!!
                    if (oldTable != table) {
                        val lines = mutableListOf<String>()
                        if (oldTable.remarks != table.remarks)
                            lines.add(
                                    "COMMENT ON TABLE $schema$quote$tableName$quote IS '${
                                        table.remarks.replace(
                                                "\\",
                                                "\\\\"
                                        )
                                    }';"
                            )
                        val oldColumns = oldTable.columns
                        val columns = table.columns
                        val oldPrimaryKeys = oldTable.primaryKeys.toMutableSet()
                        val primaryKeys = table.primaryKeys.toSet()

                        val oldColumnNames = oldColumns.map { it.columnName }
                        val columnNames = columns.map { it.columnName }
                        val dropColumnNames = oldColumnNames - columnNames.toSet()
                        if (database.dropColumnsWhenUpdate) {
                            if (dropColumnNames.isNotEmpty()) {
                                oldPrimaryKeys.removeIf { pk -> dropColumnNames.contains(pk.columnName) }
                                lines.add(
                                        "ALTER TABLE $schema$quote$tableName$quote DROP (${
                                            dropColumnNames.joinToString(
                                                    ","
                                            ) { "$quote$it$quote" }
                                        });"
                                )
                            }
                            dropFk(schema, oldColumns, dropColumnNames, lines, tableName)
                        }
                        val newColumnNames = columnNames - oldColumnNames.toSet()
                        columns.forEach { column ->
                            val columnName = column.columnName
                            if (newColumnNames.contains(columnName)) {
                                lines.add(
                                        "ALTER TABLE $schema$quote$tableName$quote ADD ${
                                            columnDef(
                                                    column,
                                                    quote
                                            )
                                        };"
                                )
                                lines.add(
                                        "COMMENT ON COLUMN $schema$quote$tableName$quote.$quote$columnName$quote IS '${
                                            column.remarks.replace(
                                                    "\\",
                                                    "\\\\"
                                            )
                                        }';"
                                )
                                addFk(schema, column, lines, tableName, columnName)
                            } else {
                                val oldColumn = oldColumns.find { it.columnName == columnName }!!
                                if (column != oldColumn) {
                                    val updateColumnDef = updateColumnDef(column, oldColumn, quote)
                                    if (updateColumnDef.isNotBlank())
                                        lines.add("ALTER TABLE $schema$quote$tableName$quote MODIFY $updateColumnDef;")
                                    if (oldColumn.remarks != column.remarks)
                                        lines.add(
                                                "COMMENT ON COLUMN $schema$quote$tableName$quote.$quote$columnName$quote IS '${
                                                    column.remarks.replace(
                                                            "\\",
                                                            "\\\\"
                                                    )
                                                }';"
                                        )
                                    updateFk(schema, column, oldColumn, lines, tableName)
                                }
                            }
                        }
                        if (oldPrimaryKeys != primaryKeys) {
                            if (oldPrimaryKeys.isNotEmpty())
                                lines.add("ALTER TABLE $schema$quote$tableName$quote DROP PRIMARY KEY;")
                            if (table.primaryKeyNames.isNotEmpty()) {
                                lines.add("ALTER TABLE $schema$quote$tableName$quote ADD PRIMARY KEY(${table.primaryKeyNames.joinToString(",") { "$quote$it$quote" }});")
                            }
                        }

                        if (database.queryIndex)
                            updateIndexes(schema, oldTable, table, lines, dropColumnNames)
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

    override fun updateColumnDef(it: Column, old: Column, quote: String): String {
        if (it.columnName.isBlank()) {
            throw Exception("${it.tableSchem ?: ""}:${it.columnName}:字段不能为空")
        }
        if (it.typeDesc.isBlank()) {
            throw Exception("${it.tableSchem ?: ""}:${it.columnName}:字段类型不能为空")
        }
        val def =
                "${if (it.typeDesc != old.typeDesc) it.typeDesc else ""}${if (it.defaultDesc != old.defaultDesc) it.defaultDesc else ""}${if (it.extra.isNotBlank() && it.extra != old.extra) " ${it.extra}" else ""}${if (it.autoIncrement) " AUTO_INCREMENT" else ""}${if (old.nullable != it.nullable) if (it.nullable) " NULL" else " NOT NULL" else ""}"
        return if (def.isBlank()) {
            ""
        } else
            "$quote${it.columnName}$quote $def"
    }

    override fun updateIndexes(
            prefixTableName: String,
            oldTable: Table,
            table: Table,
            lines: MutableList<String>,
            dropColumnNames: List<String>
    ) {
        val tableName = table.tableName
        val delIndexes = oldTable.indexes - table.indexes.toSet()
        if (delIndexes.isNotEmpty()) {
            delIndexes.forEach {
                if (!dropColumnNames.containsAll(it.columnName))
                    lines.add("DROP INDEX $quote${it.name(table.fixTableName)}$quote;")
            }
        }
        val newIndexes = table.indexes - oldTable.indexes.toSet()
        if (newIndexes.isNotEmpty()) {
            newIndexes.forEach { indexed ->
                if (indexed.unique) {
                    lines.add(
                            "CREATE UNIQUE INDEX $quote${indexed.name(table.fixTableName)}$quote ON $prefixTableName$quote$tableName$quote (${
                                indexed.columnName.joinToString(
                                        ","
                                ) { "$quote$it$quote" }
                            });"
                    )
                } else {
                    lines.add(
                            "CREATE INDEX $quote${indexed.name(table.fixTableName)}$quote ON $prefixTableName$quote$tableName$quote (${
                                indexed.columnName.joinToString(
                                        ","
                                ) { "$quote$it$quote" }
                            });"
                    )
                }
            }
        }
    }

    override fun appendTable(prefixTableName: String, table: Table, pw: Writer, database: DatabaseConfiguration) {
        val tableName = table.tableName
        pw.appendLine("$commentPrefix $tableName")
        val primaryKey = table.primaryKey
        if (primaryKey?.sequence?.isNotBlank() == true) {
            if (database.dropTablesWhenUpdate)
                pw.appendLine("DROP SEQUENCE $quote${primaryKey.sequence}$quote;")
            pw.appendLine(
                    "CREATE SEQUENCE $quote${primaryKey.sequence}$quote INCREMENT BY 1 START WITH ${primaryKey.sequenceStartWith} NOMAXVALUE NOCYCLE CACHE 10;"
            )
        }
        pw.appendLine()
        if (database.dropTablesWhenUpdate)
            pw.appendLine("DROP TABLE $prefixTableName$quote$tableName$quote;")
        pw.appendLine("CREATE TABLE $prefixTableName$quote$tableName$quote (")
        val hasPrimary = table.primaryKeyNames.isNotEmpty()
        val lastIndex = table.columns.size - 1
        table.columns.forEachIndexed { index, column ->
            pw.appendLine(
                    "  ${
                        columnDef(
                                column,
                                quote
                        )
                    }${if (index < lastIndex || hasPrimary) "," else ""}"
            )
        }
        appendKeys(table, hasPrimary, pw, quote, tableName, useForeignKey)
        pw.appendLine(");")
        appendIndexes(prefixTableName, table, pw, quote)

        pw.appendLine(
                "COMMENT ON TABLE $prefixTableName$quote$tableName$quote IS '${
                    table.remarks.replace(
                            "\\",
                            "\\\\"
                    )
                }';"
        )
        table.columns.forEach {
            pw.appendLine(
                    "COMMENT ON COLUMN $prefixTableName$quote$tableName$quote.$quote${it.columnName}$quote IS '${
                        it.remarks.replace(
                                "\\",
                                "\\\\"
                        )
                    }';"
            )
        }
        pw.appendLine()
    }


}