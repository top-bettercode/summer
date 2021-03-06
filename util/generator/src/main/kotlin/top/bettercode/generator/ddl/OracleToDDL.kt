package top.bettercode.generator.ddl

import top.bettercode.generator.GeneratorExtension
import top.bettercode.generator.database.entity.Column
import top.bettercode.generator.database.entity.Table
import java.io.Writer

object OracleToDDL : ToDDL() {
    override val quoteMark: String = "\""
    override val commentPrefix: String = "--"

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
                (oldTableNames - tableNames.toSet()).forEach { tableName ->
                    out.appendLine("$commentPrefix DROP $tableName")
                    val primaryKey = oldTables.find { it.tableName == tableName }!!.primaryKey
                    if (primaryKey?.sequence?.isNotBlank() == true)
                        out.appendLine("DROP SEQUENCE $quote${primaryKey.sequence}$quote;")
                    out.appendLine("DROP TABLE $quote$tableName$quote;")
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
                            lines.add("COMMENT ON TABLE $quote$tableName$quote IS '${table.remarks.replace("\\","\\\\")}';")
                        val oldColumns = oldTable.columns
                        val columns = table.columns
                        val oldPrimaryKeys = oldTable.primaryKeys
                        val primaryKeys = table.primaryKeys

                        if (oldPrimaryKeys.size == 1 && primaryKeys.size == 1) {
                            val oldPrimaryKey = oldPrimaryKeys[0]
                            val primaryKey = primaryKeys[0]
                            if (primaryKey.columnName != oldPrimaryKey.columnName)
                                lines.add("ALTER TABLE $quote$tableName$quote DROP PRIMARY KEY;")
                        }

                        val oldColumnNames = oldColumns.map { it.columnName }
                        val columnNames = columns.map { it.columnName }
                        val dropColumnNames = oldColumnNames - columnNames.toSet()
                        if (extension.dropColumnsWhenUpdate) {
                            if (dropColumnNames.isNotEmpty()) {
                                lines.add(
                                    "ALTER TABLE $quote$tableName$quote DROP (${
                                        dropColumnNames.joinToString(
                                            ","
                                        ) { "$quote$it$quote" }
                                    });"
                                )
                            }
                            dropFk(oldColumns, dropColumnNames, lines, tableName)
                        }
                        val newColumnNames = columnNames - oldColumnNames.toSet()
                        columns.forEach { column ->
                            val columnName = column.columnName
                            if (newColumnNames.contains(columnName)) {
                                lines.add(
                                    "ALTER TABLE $quote$tableName$quote ADD ${
                                        columnDef(
                                            column,
                                            quote
                                        )
                                    };"
                                )
                                lines.add("COMMENT ON COLUMN $quote$tableName$quote.$quote$columnName$quote IS '${column.remarks.replace("\\","\\\\")}';")
                                addFk(column, lines, tableName, columnName)
                            } else {
                                val oldColumn = oldColumns.find { it.columnName == columnName }!!
                                if (column != oldColumn) {
                                    val updateColumnDef = updateColumnDef(column, oldColumn, quote)
                                    if (updateColumnDef.isNotBlank())
                                        lines.add("ALTER TABLE $quote$tableName$quote MODIFY $updateColumnDef;")
                                    if (oldColumn.remarks != column.remarks)
                                        lines.add("COMMENT ON COLUMN $quote$tableName$quote.$quote$columnName$quote IS '${column.remarks.replace("\\","\\\\")}';")
                                    updateFk(column, oldColumn, lines, tableName)
                                }
                            }
                        }
                        if (oldPrimaryKeys.size == 1 && primaryKeys.size == 1) {
                            val oldPrimaryKey = oldPrimaryKeys[0]
                            val primaryKey = primaryKeys[0]
                            if (primaryKey.columnName != oldPrimaryKey.columnName)
                                lines.add("ALTER TABLE $quote$tableName$quote ADD PRIMARY KEY(\"$quote${primaryKey.columnName}$quote\" )")
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

    override fun updateColumnDef(it: Column, old: Column, quote: String): String {
        if (it.columnName.isBlank()) {
            throw Exception("${it.tableSchem ?: ""}:${it.columnName}:??????????????????")
        }
        if (it.typeDesc.isBlank()) {
            throw Exception("${it.tableSchem ?: ""}:${it.columnName}:????????????????????????")
        }
        val def =
            "${if (it.typeDesc != old.typeDesc) it.typeDesc else ""}${if (it.defaultDesc != old.defaultDesc) it.defaultDesc else ""}${if (it.extra.isNotBlank() && it.extra != old.extra) " ${it.extra}" else ""}${if (it.autoIncrement && (it.autoIncrement != old.autoIncrement)) " AUTO_INCREMENT" else ""}${if (old.nullable != it.nullable) if (it.nullable) " NULL" else " NOT NULL" else ""}"
        return if (def.isBlank()) {
            ""
        } else
            "$quote${it.columnName}$quote $def"
    }

    override fun updateIndexes(
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
                    lines.add("DROP INDEX $quote${it.name}$quote;")
            }
        }
        val newIndexes = table.indexes - oldTable.indexes.toSet()
        if (newIndexes.isNotEmpty()) {
            newIndexes.forEach { indexed ->
                if (indexed.unique) {
                    lines.add(
                        "CREATE UNIQUE INDEX $quote${indexed.name}$quote ON $quote$tableName$quote (${
                            indexed.columnName.joinToString(
                                ","
                            ) { "$quote$it$quote" }
                        });"
                    )
                } else {
                    lines.add(
                        "CREATE INDEX $quote${indexed.name}$quote ON $quote$tableName$quote (${
                            indexed.columnName.joinToString(
                                ","
                            ) { "$quote$it$quote" }
                        });"
                    )
                }
            }
        }
    }

    override fun appendTable(table: Table, pw: Writer) {
        val tableName = table.tableName
        pw.appendLine("$commentPrefix $tableName")
        val primaryKey = table.primaryKey
        if (primaryKey?.sequence?.isNotBlank() == true) {
            if (table.ext.dropTablesWhenUpdate)
                pw.appendLine("DROP SEQUENCE $quote${primaryKey.sequence}$quote;")
            pw.appendLine(
                "CREATE SEQUENCE $quote${primaryKey.sequence}$quote INCREMENT BY 1 START WITH ${primaryKey.sequenceStartWith} NOMAXVALUE NOCYCLE CACHE 10;"
            )
        }
        pw.appendLine()
        if (table.ext.dropTablesWhenUpdate)
            pw.appendLine("DROP TABLE $quote$tableName$quote;")
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
                }${if (index < lastIndex || hasPrimary) "," else ""}"
            )
        }
        appendKeys(table, hasPrimary, pw, quote, tableName, useForeignKey)
        pw.appendLine(");")
        appendIndexes(table, pw, quote)

        pw.appendLine("COMMENT ON TABLE $quote$tableName$quote IS '${table.remarks.replace("\\","\\\\")}';")
        table.columns.forEach {
            pw.appendLine("COMMENT ON COLUMN $quote$tableName$quote.$quote${it.columnName}$quote IS '${it.remarks.replace("\\","\\\\")}';")
        }
        pw.appendLine()
    }


}