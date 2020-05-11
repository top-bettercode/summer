package cn.bestwu.generator.puml

import cn.bestwu.generator.DataType
import cn.bestwu.generator.DatabaseDriver
import cn.bestwu.generator.GeneratorExtension
import cn.bestwu.generator.database.entity.Column
import cn.bestwu.generator.database.entity.Indexed
import cn.bestwu.generator.database.entity.Table
import cn.bestwu.generator.ddl.MysqlToDDL
import cn.bestwu.generator.ddl.OracleToDDL
import cn.bestwu.generator.dom.java.JavaTypeResolver
import cn.bestwu.generator.dsl.def.PlantUML
import cn.bestwu.generator.powerdesigner.PdmReader
import java.io.File

/**
 * @author Peter Wu
 * @since 0.0.45
 */
object PumlConverter {

    fun toTables(puml: File, call: (Table) -> Unit = {}): List<Table> {
        return toTableOrAnys(puml, call).asSequence().filter { it is Table }.map { it as Table }.toList()
    }

    fun toTableOrAnys(puml: File, call: (Table) -> Unit = {}): List<Any> {
        val tables = mutableListOf<Any>()
        var remarks = ""
        var primaryKeyNames = mutableListOf<String>()
        var indexes = mutableListOf<Indexed>()
        var pumlColumns = mutableListOf<Any>()
        var tableName = ""
        var desc = "<<(T,#DDDDDD)>>"
        var sequenceStartWith: Int? = null
        var isField = false
        var isUml = false
        var moduleName: String? = null
        puml.readLines().forEach {
            if (it.isNotBlank()) {
                val line = it.trim()
                if (line.startsWith("@startuml")) {
                    moduleName = line.substringAfter("@startuml").trim()
                    isUml = true
                } else if (line.startsWith("class ")) {
                    val fieldDef = line.split(" ")
                    tableName = fieldDef[1].trim()
                    if (fieldDef.size > 3) {
                        val tableDesc = fieldDef[2]
                        desc = tableDesc
                        sequenceStartWith = tableDesc.replace(Regex("<<\\(T,#DDDDD(\\d)\\)>>"), "$1").toIntOrNull()
                    }
                } else if (tableName.isNotBlank() && !isField) {
                    if ("==" == line)
                        isField = true
                    else
                        remarks = line
                } else if (isField) {
                    val uniqueMult = line.startsWith("'UNIQUE")
                    if (uniqueMult || line.startsWith("'INDEX")) {
                        val columnNames = line.substringAfter(if (uniqueMult) "'UNIQUE" else "'INDEX").trim()
                        indexes.add(Indexed("${if (uniqueMult) "UK" else "IDX"}_${tableName.replace("_", "").takeLast(7)}_${columnNames.replace(tableName, "").replace("_", "").replace(",", "").takeLast(7)}", uniqueMult, columnNames.split(",").toMutableList()))
                    } else if ("}" == line) {
                        val table = Table(productName = DataType.PUML.name, catalog = null, schema = null, tableName = tableName, tableType = "", remarks = remarks, primaryKeyNames = primaryKeyNames, indexes = indexes, pumlColumns = pumlColumns, desc = desc, sequenceStartWith = sequenceStartWith, moduleName = moduleName
                                ?: "")
                        call(table)
                        tables.add(table)

                        primaryKeyNames = mutableListOf()
                        indexes = mutableListOf()
                        pumlColumns = mutableListOf()
                        tableName = ""
                        remarks = ""
                        desc = "<<(T,#DDDDDD)>>"
                        sequenceStartWith = null
                        isField = false
                    } else if (!line.startsWith("'")) {
                        val lineDef = line.trim().split("--")
                        val fieldDef = lineDef[0].trim()
                        val fieldDefs = fieldDef.split("[ :]".toRegex())
                        val columnName = fieldDefs[0]
                        val columnDef = fieldDef.substringAfter(columnName).replace(":", "").trim()
                        val type = columnDef.split(" ")[0].trim()
                        var extra = columnDef.substringAfter(type)
                        val (columnSize, decimalDigits) = parseType(type)
                        var defaultVal: String? = null
                        if (columnDef.contains("DEFAULT")) {
                            defaultVal = columnDef.substringAfter("DEFAULT").trim().substringBefore(" ").trim('\'').trim()
                            extra = extra.replace(Regex(" DEFAULT +'?$defaultVal'?"), "")
                        }
                        var fk = false
                        var refTable: String? = null
                        var refColumn: String? = null
                        if (columnDef.contains("FK")) {//FK > docs.id
                            val ref = columnDef.substringAfter("FK >").trim().substringBefore(" ").trim()
                            extra = extra.replace(Regex(" FK > +$ref"), "")
                            val refs = ref.split(".")
                            fk = true
                            refTable = refs[0]
                            refColumn = refs[1]
                        }
                        val typeName = type.substringBefore("(")
                        val unique = columnDef.contains("UNIQUE")
                        val indexed = columnDef.contains("INDEX")
                        val autoIncrement = columnDef.contains("AUTO_INCREMENT")
                        extra = extra.replace(" UNIQUE", "", true)
                        extra = extra.replace(" INDEX", "", true)
                        extra = extra.replace(" AUTO_INCREMENT", "", true)
                        extra = extra.replace(" NOT NULL", "", true)
                        extra = extra.replace(" NULL", "", true)
                        extra = extra.replace(" PK", "", true).trim()
                        val column = Column(tableCat = null, columnName = columnName, remarks = lineDef.last().trim(), typeName = typeName, dataType = JavaTypeResolver.calculateDataType(typeName), columnSize = columnSize, decimalDigits = decimalDigits, nullable = !columnDef.contains("NOT NULL"), unique = unique, indexed = indexed, columnDef = defaultVal, extra = extra, tableSchem = null, isForeignKey = fk, pktableName = refTable, pkcolumnName = refColumn, autoIncrement = autoIncrement)
                        if (unique)
                            indexes.add(Indexed("UK_${tableName.replace("_", "").takeLast(7)}_${columnName.replace(tableName, "").replace("_", "").replace(",", "").takeLast(7)}", true, mutableListOf(columnName)))
                        if (indexed)
                            indexes.add(Indexed("IDX_${tableName.replace("_", "").takeLast(7)}_${columnName.replace(tableName, "").replace("_", "").replace(",", "").takeLast(7)}", false, mutableListOf(columnName)))
                        if (columnDef.contains("PK")) {
                            primaryKeyNames.add(column.columnName)
                        }
                        pumlColumns.add(column)
                    } else {
                        pumlColumns.add(line)
                    }
                } else if (line.startsWith("@enduml")) {
                    isUml = false
                } else if (isUml && line.isNotBlank() && !line.matches(Regex("^.* \"1\" -- \"0\\.\\.\\*\" .*$"))) {
                    tables.add(line)
                }
            }
        }

        return tables
    }

    fun parseType(type: String): Pair<Int, Int> {
        var columnSize = 0
        var decimalDigits = 0
        if (type.contains("(")) {
            val lengthScale = type.substringAfter("(").substringBefore(")")
            if (lengthScale.contains(",")) {
                val ls = lengthScale.split(",")
                columnSize = ls[0].toInt()
                decimalDigits = ls[1].toInt()
            } else {
                columnSize = lengthScale.toInt()
            }
        }
        return Pair(columnSize, decimalDigits)
    }

    private fun compile(extension: GeneratorExtension, tables: List<Any>, out: File) {
        if (tables.isNotEmpty()) {
            val any = tables[0]
            val plantUML = PlantUML(if (any is Table) any.moduleName else null, out.absolutePath)
            plantUML.setUp()
            tables.forEach {
                if (it is Table) {
                    plantUML.call(extension, it)
                } else {
                    plantUML.appendlnText(it.toString())
                }
            }
            plantUML.tearDown()
        }
    }

    fun reformat(extension: GeneratorExtension) {
        extension.pumlSrcSources.forEach {
            when (extension.pumlDatabaseDriver) {
                DatabaseDriver.MYSQL -> toMysql(extension, it, it)
                DatabaseDriver.ORACLE -> toOracle(extension, it, it)
                else -> compile(extension, it, it)
            }
        }
    }

    fun compile(extension: GeneratorExtension, src: File, out: File) {
        compile(extension, toTableOrAnys(src), out)
    }

    fun toMysql(extension: GeneratorExtension, src: File, out: File) {
        val tables = toTableOrAnys(src)
        tables.forEach { t ->
            if (t is Table) {
                t.pumlColumns.forEach {
                    if (it is Column) {
                        when (it.typeName) {
                            "VARCHAR2" -> it.typeName = "VARCHAR"
                            "RAW" -> it.typeName = "BINARY"
                            "CLOB" -> it.typeName = "TEXT"
                            "NUMBER" -> {
                                if (it.decimalDigits == 0) {
                                    when (it.columnSize) {
                                        in 1..3 -> {
                                            it.typeName = "TINYINT"
                                        }
                                        in 4..5 -> {
                                            it.typeName = "SMALLINT"
                                        }
                                        in 6..7 -> {
                                            it.typeName = "MEDUIMINT"
                                        }
                                        in 8..10 -> {
                                            it.typeName = "INT"
                                        }
                                        in 11..20 -> {
                                            it.typeName = "BIGINT"
                                        }
                                        else -> it.typeName = "DECIMAL"
                                    }
                                } else {
                                    it.typeName = "DECIMAL"
                                }
                            }
                        }
                    }
                }
            }
        }
        compile(extension, tables, out)
    }

    fun toOracle(extension: GeneratorExtension, src: File, out: File) {
        val tables = toTableOrAnys(src)
        tables.forEach { t ->
            if (t is Table) {
                t.pumlColumns.forEach {
                    if (it is Column) {
                        when (it.typeName) {
                            "VARCHAR" -> it.typeName = "VARCHAR2"
                            "TINYINT" -> {
                                it.typeName = "NUMBER"
                            }
                            "SMALLINT" -> {
                                it.typeName = "NUMBER"
                            }
                            "MEDUIMINT" -> {
                                it.typeName = "NUMBER"
                            }
                            "INT" -> {
                                it.typeName = "NUMBER"
                            }
                            "BIGINT" -> {
                                it.typeName = "NUMBER"
                            }
                            "FLOAT", "DOUBLE", "DECIMAL" -> {
                                it.typeName = "NUMBER"
                            }
                            "TINYTEXT" -> it.typeName = "CLOB"
                            "TINYBLOB" -> it.typeName = "BLOB"
                            "BINARY" -> it.typeName = "RAW"
                            "TEXT" -> it.typeName = "CLOB"
                            "LONGTEXT" -> it.typeName = "CLOB"
                        }
                    }
                }
            }
        }
        compile(extension, tables, out)
    }

    fun toDDL(extension: GeneratorExtension) {
        MysqlToDDL.useQuote = extension.sqlQuote
        OracleToDDL.useQuote = extension.sqlQuote
        MysqlToDDL.useForeignKey = extension.useForeignKey
        OracleToDDL.useForeignKey = extension.useForeignKey
        when (extension.dataType) {
            DataType.PUML -> {
                extension.pumlSrcSources.forEach {
                    when (extension.pumlDatabaseDriver) {
                        DatabaseDriver.MYSQL -> MysqlToDDL.toDDL(toTables(it), extension.pumlSqlOutputFile(it, extension.file(extension.pumlSrc)))
                        DatabaseDriver.ORACLE -> OracleToDDL.toDDL(toTables(it), extension.pumlSqlOutputFile(it, extension.file(extension.pumlSrc)))
                        else -> {
                            throw IllegalArgumentException("不支持的数据库")
                        }
                    }
                }
            }
            DataType.PDM -> {
                val pdmFile = extension.file(extension.pdmSrc)
                when (extension.pumlDatabaseDriver) {
                    DatabaseDriver.MYSQL -> MysqlToDDL.toDDL(PdmReader.read(pdmFile), extension.pumlSqlOutputFile(pdmFile, pdmFile.parentFile))
                    DatabaseDriver.ORACLE -> OracleToDDL.toDDL(PdmReader.read(pdmFile), extension.pumlSqlOutputFile(pdmFile, pdmFile.parentFile))
                    else -> {
                        throw IllegalArgumentException("不支持的数据库")
                    }
                }
            }
            else -> {
                throw IllegalArgumentException("不支持数据结构源")
            }
        }
    }

    fun toDDLUpdate(extension: GeneratorExtension) {
        MysqlToDDL.useQuote = extension.sqlQuote
        OracleToDDL.useQuote = extension.sqlQuote
        MysqlToDDL.useForeignKey = extension.useForeignKey
        OracleToDDL.useForeignKey = extension.useForeignKey
        extension.pumlSqlUpdateOutputFile().printWriter().use { pw ->
            val deleteTablesWhenUpdate = extension.deleteTablesWhenUpdate
            val databaseFile = extension.file(extension.pumlDatabase + "/database.puml")

            val allTables = mutableListOf<Table>()
            when (extension.dataType) {
                DataType.PUML -> {
                    extension.pumlSrcSources.forEach { file ->
                        val tables = toTables(file)
                        allTables.addAll(tables)
                        when (extension.pumlDatabaseDriver) {
                            DatabaseDriver.MYSQL -> MysqlToDDL.toDDLUpdate(if (deleteTablesWhenUpdate) oldTables(extension, databaseFile) else oldTables(extension, databaseFile, tables.map { it.tableName }.toTypedArray()), tables, pw, deleteTablesWhenUpdate)
                            DatabaseDriver.ORACLE -> OracleToDDL.toDDLUpdate(if (deleteTablesWhenUpdate) oldTables(extension, databaseFile) else oldTables(extension, databaseFile, tables.map { it.tableName }.toTypedArray()), tables, pw, deleteTablesWhenUpdate)
                            else -> {
                                throw IllegalArgumentException("不支持的数据库")
                            }
                        }
                    }
                }
                DataType.PDM -> {
                    val pdmFile = extension.file(extension.pdmSrc)
                    val tables = PdmReader.read(pdmFile)
                    allTables.addAll(tables)
                    when (extension.pumlDatabaseDriver) {
                        DatabaseDriver.MYSQL -> MysqlToDDL.toDDLUpdate(if (deleteTablesWhenUpdate) oldTables(extension, databaseFile) else oldTables(extension, databaseFile, tables.map { it.tableName }.toTypedArray()), tables, pw, deleteTablesWhenUpdate)
                        DatabaseDriver.ORACLE -> OracleToDDL.toDDLUpdate(if (deleteTablesWhenUpdate) oldTables(extension, databaseFile) else oldTables(extension, databaseFile, tables.map { it.tableName }.toTypedArray()), tables, pw, deleteTablesWhenUpdate)
                        else -> {
                            throw IllegalArgumentException("不支持的数据库")
                        }
                    }
                }
                else -> {
                    throw IllegalArgumentException("不支持数据结构源")
                }
            }
            if (DataType.PUML == extension.updateFromType)
                compile(extension, allTables, databaseFile)
        }

    }

    private fun oldTables(extension: GeneratorExtension, databaseFile: File, tableNameList: Array<String>? = null): List<Table> {
        return if (DataType.PUML == extension.updateFromType && databaseFile.exists()) {
            toTables(databaseFile)
        } else {
            extension.use { extension.tables(tableNameList ?: tableNames().toTypedArray()) }
        }
    }

}
