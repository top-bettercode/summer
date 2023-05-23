package top.bettercode.summer.tools.generator.ddl

import top.bettercode.summer.tools.generator.database.entity.Column
import top.bettercode.summer.tools.generator.database.entity.Table
import top.bettercode.summer.tools.generator.dom.unit.FileUnit
import java.io.Writer

/**
 *
 * @author Peter Wu
 */
abstract class ToDDL : IToDDL {
    override var useForeignKey: Boolean = false
    override var useQuote: Boolean = false
    protected val quote: String by lazy { if (useQuote) quoteMark else "" }

    protected open fun columnDef(it: Column, quote: String): String {
        if (it.columnName.isBlank()) {
            throw Exception("${it.tableSchem ?: ""}:${it.columnName}:字段不能为空")
        }
        if (it.typeDesc.isBlank()) {
            throw Exception("${it.tableSchem ?: ""}:${it.columnName}:字段类型不能为空")
        }
        return "$quote${it.columnName}$quote ${it.typeDesc}${if (it.unsigned) " UNSIGNED" else ""}${it.defaultDesc}${if (it.extra.isNotBlank()) " ${it.extra}" else ""}${if (it.autoIncrement) " AUTO_INCREMENT" else ""}${if (it.nullable) " NULL" else " NOT NULL"}"
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

    protected open fun appendKeys(
            table: Table,
            hasPrimary: Boolean,
            pw: Writer,
            quote: String,
            tableName: String,
            useForeignKey: Boolean = false
    ) {
        val fks = table.columns.filter { it.isForeignKey }
        if (hasPrimary)
            pw.appendln("  PRIMARY KEY (${table.primaryKeyNames.joinToString(",") { "$quote$it$quote" }})${if (useForeignKey && fks.isNotEmpty()) "," else ""}")
        if (useForeignKey) {
            val lastFksIndex = fks.size - 1
            fks.forEachIndexed { index, column ->
                val columnName = column.columnName
                pw.appendln(
                        "  CONSTRAINT ${
                            foreignKeyName(
                                    tableName,
                                    columnName
                            )
                        } FOREIGN KEY ($quote$columnName$quote) REFERENCES $quote${column.pktableName}$quote ($quote${column.pkcolumnName}$quote)${if (index < lastFksIndex) "," else ""}"
                )
            }
        }
    }

    protected fun foreignKeyName(tableName: String, columnName: String) =
            "${quote}FK_${tableName.replace("_", "").takeLast(7)}_${
                columnName.replace(tableName, "").replace("_", "").replace(",", "").takeLast(7)
            }$quote"


    protected fun appendIndexes(prefixTableName: String, table: Table, pw: Writer, quote: String) {
        val tableName = table.tableName
        table.indexes.forEach { t ->
            if (t.unique) {
                pw.appendln(
                        "CREATE UNIQUE INDEX ${t.name(table.fixTableName)} ON $prefixTableName$quote$tableName$quote (${
                            t.columnName.joinToString(
                                    ","
                            ) { "$quote$it$quote" }
                        });"
                )
            } else {
                pw.appendln(
                        "CREATE INDEX ${t.name(table.fixTableName)} ON $prefixTableName$quote$tableName$quote (${
                            t.columnName.joinToString(
                                    ","
                            ) { "$quote$it$quote" }
                        });"
                )
            }
        }
    }

    protected open fun updateIndexes(
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
                    lines.add("DROP INDEX $quote${it.name(table.fixTableName)}$quote ON $prefixTableName$quote$tableName$quote;")
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

    protected fun updateFk(
            prefixTableName: String,
            column: Column,
            oldColumn: Column,
            lines: MutableList<String>,
            tableName: String
    ) {
        if (useForeignKey && (column.isForeignKey != oldColumn.isForeignKey || column.pktableName != oldColumn.pktableName || column.pkcolumnName != oldColumn.pkcolumnName)) {
            if (oldColumn.isForeignKey) {
                lines.add(dropFkStatement(prefixTableName, tableName, oldColumn.columnName))
            }
            if (column.isForeignKey) {
                lines.add(
                        "ALTER TABLE $prefixTableName$quote$tableName$quote ADD CONSTRAINT ${
                            foreignKeyName(
                                    tableName,
                                    column.columnName
                            )
                        } FOREIGN KEY ($quote${column.columnName}$quote) REFERENCES $quote${column.pktableName}$quote ($quote${column.pkcolumnName}$quote);"
                )
            }
        }
    }

    protected fun addFk(
            prefixTableName: String,
            column: Column,
            lines: MutableList<String>,
            tableName: String,
            columnName: String
    ) {
        if (useForeignKey && column.isForeignKey)
            lines.add(
                    "ALTER TABLE $prefixTableName$quote$tableName$quote ADD CONSTRAINT ${
                        foreignKeyName(
                                tableName,
                                column.columnName
                        )
                    } FOREIGN KEY ($quote$columnName$quote) REFERENCES $quote${column.pktableName}$quote ($quote${column.pkcolumnName}$quote);"
            )
    }

    protected fun dropFk(
            prefixTableName: String,
            oldColumns: MutableList<Column>,
            dropColumnNames: List<String>,
            lines: MutableList<String>,
            tableName: String
    ) {
        if (useForeignKey)
            oldColumns.filter { it.isForeignKey && dropColumnNames.contains(it.columnName) }
                    .forEach { column ->
                        lines.add(dropFkStatement(prefixTableName, tableName, column.columnName))
                    }
    }

    protected open fun dropFkStatement(
            prefixTableName: String,
            tableName: String,
            columnName: String
    ) =
            "ALTER TABLE $prefixTableName$quote$tableName$quote DROP CONSTRAINT ${
                foreignKeyName(
                        tableName,
                        columnName
                )
            };"

    override fun toDDL(tables: List<Table>, out: FileUnit) {
        if (tables.isNotEmpty()) {
            out.use { pw ->
                pw.appendln("$commentPrefix ${tables[0].subModule}")
                tables.forEach { table ->
                    appendTable("", table, pw)
                }
            }
        }
    }
}
