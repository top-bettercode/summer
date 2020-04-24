package cn.bestwu.generator.ddl

import cn.bestwu.generator.database.domain.Column
import cn.bestwu.generator.database.domain.Table
import java.io.PrintWriter

object OracleToDDL : ToDDL() {
    override val quoteMark: String = "\""
    override val commentPrefix: String = "--"

    override fun toDDLUpdate(oldTables: List<Table>, tables: List<Table>, out: PrintWriter, deleteTablesWhenUpdate: Boolean) {
        if (tables != oldTables) {
            val tableNames = tables.map { it.tableName }
            val oldTableNames = oldTables.map { it.tableName }
            if (deleteTablesWhenUpdate)
                (oldTableNames - tableNames).forEach { tableName ->
                    out.println("$commentPrefix DROP $tableName")
                    if (oldTables.find { it.tableName == tableName }!!.sequenceStartWith != null)
                        out.println("DROP SEQUENCE $quote${tableName}_S$quote;")
                    out.println("DROP TABLE $quote$tableName$quote;")
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
                            out.println("COMMENT ON TABLE $quote$tableName$quote IS '${table.remarks}';")
                        val oldColumns = oldTable.columns
                        val columns = table.columns
                        val oldPrimaryKeys = oldTable.primaryKeys
                        val primaryKeys = table.primaryKeys
                        val oldPrimaryKey = oldPrimaryKeys[0]
                        val primaryKey = primaryKeys[0]
                        if (oldPrimaryKeys.size == 1 && primaryKeys.size == 1 && oldPrimaryKey != primaryKey) {
                            if (primaryKey.columnName != oldPrimaryKey.columnName)
                                out.println("ALTER TABLE $quote$tableName$quote DROP PRIMARY KEY;")
                        }

                        updateIndexes(oldTable, table, out)

                        val oldColumnNames = oldColumns.map { it.columnName }
                        val columnNames = columns.map { it.columnName }
                        val dropColumnNames = oldColumnNames - columnNames
                        if (dropColumnNames.isNotEmpty()) {
                            out.println("ALTER TABLE $quote$tableName$quote DROP (${dropColumnNames.joinToString(",") { "$quote$it$quote" }});")
                        }
                        dropFk(oldColumns, dropColumnNames, out, tableName)
                        val newColumnNames = columnNames - oldColumnNames
                        columns.forEach { column ->
                            val columnName = column.columnName
                            if (newColumnNames.contains(columnName)) {
                                out.println("ALTER TABLE $quote$tableName$quote ADD ${columnDef(column, quote)};")
                                out.println("COMMENT ON COLUMN $quote$tableName$quote.$quote$columnName$quote IS '${column.remarks}';")
                                addFk(column, out, tableName, columnName)
                            } else {
                                val oldColumn = oldColumns.find { it.columnName == columnName }!!
                                if (column != oldColumn) {
                                    val updateColumnDef = updateColumnDef(column, oldColumn, quote)
                                    if (updateColumnDef.isNotBlank())
                                        out.println("ALTER TABLE $quote$tableName$quote MODIFY $updateColumnDef;")
                                    if (oldColumn.remarks != column.remarks)
                                        out.println("COMMENT ON COLUMN $quote$tableName$quote.$quote$columnName$quote IS '${column.remarks}';")
                                    updateFk(column, oldColumn, out, tableName)
                                }
                            }
                        }
                        if (oldPrimaryKeys.size == 1 && primaryKeys.size == 1 && oldPrimaryKey != primaryKey) {
                            if (primaryKey.columnName != oldPrimaryKey.columnName)
                                out.println("ALTER TABLE $quote$tableName$quote ADD PRIMARY KEY(\"$quote${primaryKey.columnName}$quote\" )")
                        }

                        out.println()
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
        val def = "${if (it.typeDesc != old.typeDesc) it.typeDesc else ""}${if (it.defaultDesc != old.defaultDesc) it.defaultDesc else ""}${if (it.extra.isNotBlank() && it.extra != old.extra) " ${it.extra}" else ""}${if (it.autoIncrement && it.autoIncrement != old.autoIncrement) " AUTO_INCREMENT" else ""}${if (old.nullable != it.nullable) if (it.nullable) " NULL" else " NOT NULL" else ""}"
        return if (def.isBlank()) {
            ""
        } else
            "$quote${it.columnName}$quote $def"
    }

    override fun updateIndexes(oldTable: Table, table: Table, out: PrintWriter) {
        val tableName = table.tableName
        val delIndexes = oldTable.indexes - table.indexes
        if (delIndexes.isNotEmpty()) {
            delIndexes.forEach {
                out.println("DROP INDEX $quote${it.name}$quote;")
            }
        }
        val newIndexes = table.indexes - oldTable.indexes
        if (newIndexes.isNotEmpty()) {
            newIndexes.forEach { indexed ->
                if (indexed.unique) {
                    out.println("CREATE UNIQUE INDEX $quote${indexed.name}$quote ON $quote$tableName$quote (${indexed.columnName.joinToString(",") { "$quote$it$quote" }});")
                } else {
                    out.println("CREATE INDEX $quote${indexed.name}$quote ON $quote$tableName$quote (${indexed.columnName.joinToString(",") { "$quote$it$quote" }});")
                }
            }
        }
    }

    override fun appendTable(table: Table, pw: PrintWriter) {
        val tableName = table.tableName
        pw.println("$commentPrefix $tableName")
        if (table.sequenceStartWith != null) {
            pw.println("DROP SEQUENCE $quote${tableName}_S$quote;")
            pw.println("CREATE SEQUENCE $quote${tableName}_S$quote INCREMENT BY 1 START WITH 1${fill(table.sequenceStartWith!!)};")
        }
        pw.println()
        pw.println("DROP TABLE $quote$tableName$quote;")
        pw.println("CREATE TABLE $quote$tableName$quote (")
        val hasPrimary = table.primaryKeyNames.isNotEmpty()
        val lastIndex = table.columns.size - 1
        table.columns.forEachIndexed { index, column ->
            pw.println("  ${columnDef(column, quote)}${if (index < lastIndex || hasPrimary) "," else ""}")
        }
        appendKeys(table, hasPrimary, pw, quote, tableName, useForeignKey)
        pw.println(");")
        appendIndexes(table, pw, quote)

        pw.println("COMMENT ON TABLE $quote$tableName$quote IS '${table.remarks}';")
        table.columns.forEach {
            pw.println("COMMENT ON COLUMN $quote$tableName$quote.$quote${it.columnName}$quote IS '${it.remarks}';")
        }
        pw.println()
    }


    private fun fill(length: Int): String {
        var str = ""
        for (i in 1..length) {
            str += "0"
        }
        return str
    }

}