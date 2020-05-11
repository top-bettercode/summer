package cn.bestwu.generator.ddl

import cn.bestwu.generator.database.entity.Column
import cn.bestwu.generator.database.entity.Table
import java.io.File
import java.io.PrintWriter

/**
 *
 * @author Peter Wu
 */
abstract class ToDDL : IToDDL {
    override var useForeignKey: Boolean = false
    override var useQuote: Boolean = false
    protected val quote: String
        get() = if (useQuote) quoteMark else ""

    protected fun columnDef(it: Column, quote: String): String {
        if (it.columnName.isBlank()) {
            throw Exception("${it.tableSchem ?: ""}:${it.columnName}:字段不能为空")
        }
        if (it.typeDesc.isBlank()) {
            throw Exception("${it.tableSchem ?: ""}:${it.columnName}:字段类型不能为空")
        }
        return "$quote${it.columnName}$quote ${it.typeDesc}${it.defaultDesc}${if (it.extra.isNotBlank()) " ${it.extra}" else ""}${if (it.autoIncrement) " AUTO_INCREMENT" else ""}${if (it.nullable) " NULL" else " NOT NULL"}"
    }

    protected open fun updateColumnDef(it: Column, old: Column, quote: String): String {
        if (it.columnName.isBlank()) {
            throw Exception("${it.tableSchem ?: ""}:${it.columnName}:字段不能为空")
        }
        if (it.typeDesc.isBlank()) {
            throw Exception("${it.tableSchem ?: ""}:${it.columnName}:字段类型不能为空")
        }
        return "$quote${it.columnName}$quote ${it.typeDesc}${it.defaultDesc}${if (it.extra.isNotBlank()) " ${it.extra}" else ""}${if (it.autoIncrement) " AUTO_INCREMENT" else ""}${if (it.nullable) " NULL" else " NOT NULL"}"
    }

    protected fun appendKeys(table: Table, hasPrimary: Boolean, pw: PrintWriter, quote: String, tableName: String, useForeignKey: Boolean = false) {
        val fks = table.columns.filter { it.isForeignKey }
        if (hasPrimary)
            pw.println("  PRIMARY KEY (${table.primaryKeyNames.joinToString(",") { "$quote$it$quote" }})${if (useForeignKey && fks.isNotEmpty()) "," else ""}")
        if (useForeignKey) {
            val lastFksIndex = fks.size - 1
            fks.forEachIndexed { index, column ->
                val columnName = column.columnName
                pw.println("  CONSTRAINT ${foreignKeyName(tableName, columnName)} FOREIGN KEY ($quote$columnName$quote) REFERENCES $quote${column.pktableName}$quote ($quote${column.pkcolumnName}$quote)${if (index < lastFksIndex) "," else ""}")
            }
        }
    }

    protected fun foreignKeyName(tableName: String, columnName: String) =
            "${quote}FK_${tableName.replace("_", "").takeLast(7)}_${columnName.replace(tableName, "").replace("_", "").replace(",", "").takeLast(7)}$quote"


    protected fun appendIndexes(table: Table, pw: PrintWriter, quote: String) {
        val tableName = table.tableName
        table.indexes.forEach { t ->
            if (t.unique) {
                pw.println("CREATE UNIQUE INDEX ${t.name} ON $quote$tableName$quote (${t.columnName.joinToString(",") { "$quote$it$quote" }});")
            } else {
                pw.println("CREATE INDEX ${t.name} ON $quote$tableName$quote (${t.columnName.joinToString(",") { "$quote$it$quote" }});")
            }
        }
    }

    protected open fun updateIndexes(oldTable: Table, table: Table, out: PrintWriter) {
        val tableName = table.tableName
        val delIndexes = oldTable.indexes - table.indexes
        if (delIndexes.isNotEmpty()) {
            delIndexes.forEach {
                out.println("DROP INDEX $quote${it.name}$quote ON $quote$tableName$quote;")
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

    protected fun updateFk(column: Column, oldColumn: Column, out: PrintWriter, tableName: String) {
        if (useForeignKey && (column.isForeignKey != oldColumn.isForeignKey || column.pktableName != oldColumn.pktableName || column.pkcolumnName != oldColumn.pkcolumnName)) {
            if (oldColumn.isForeignKey) {
                out.println(dropFkStatement(tableName, oldColumn.columnName))
            }
            if (column.isForeignKey) {
                out.println("ALTER TABLE $quote$tableName$quote ADD CONSTRAINT ${foreignKeyName(tableName, column.columnName)} FOREIGN KEY ($quote${column.columnName}$quote) REFERENCES $quote${column.pktableName}$quote ($quote${column.pkcolumnName}$quote);")
            }
        }
    }

    protected fun addFk(column: Column, out: PrintWriter, tableName: String, columnName: String) {
        if (useForeignKey && column.isForeignKey)
            out.println("ALTER TABLE $quote$tableName$quote ADD CONSTRAINT ${foreignKeyName(tableName, column.columnName)} FOREIGN KEY ($quote$columnName$quote) REFERENCES $quote${column.pktableName}$quote ($quote${column.pkcolumnName}$quote);")
    }

    protected fun dropFk(oldColumns: MutableList<Column>, dropColumnNames: List<String>, out: PrintWriter, tableName: String) {
        if (useForeignKey)
            oldColumns.filter { it.isForeignKey && dropColumnNames.contains(it.columnName) }.forEach { column ->
                out.println(dropFkStatement(tableName, column.columnName))
            }
    }

    protected open fun dropFkStatement(tableName: String, columnName: String) =
            "ALTER TABLE $quote$tableName$quote DROP CONSTRAINT ${foreignKeyName(tableName, columnName)};"

    override fun toDDL(tables: List<Table>, out: File) {
        if (tables.isNotEmpty()) {
            out.printWriter().use { pw ->
                pw.println("$commentPrefix ${tables[0].moduleName}")
                tables.forEach { table ->
                    appendTable(table, pw)
                }
            }
        }
    }
}
