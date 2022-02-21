package top.bettercode.generator.puml

import top.bettercode.generator.DataType
import top.bettercode.generator.GeneratorExtension
import top.bettercode.generator.database.entity.Column
import top.bettercode.generator.database.entity.Indexed
import top.bettercode.generator.database.entity.Table
import top.bettercode.generator.ddl.MysqlToDDL
import top.bettercode.generator.ddl.OracleToDDL
import top.bettercode.generator.ddl.SqlLiteToDDL
import top.bettercode.generator.dom.java.JavaTypeResolver
import top.bettercode.generator.dsl.def.PlantUML
import top.bettercode.generator.powerdesigner.PdmReader
import java.io.File
import java.util.*

/**
 * @author Peter Wu
 * @since 0.0.45
 */
object PumlConverter {

    fun toTables(puml: File, call: (Table) -> Unit = {}): List<Table> {
        return toTableOrAnys(puml, call).asSequence().filter { it is Table }.map { it as Table }
            .toList()
    }

    private fun toTableOrAnys(puml: File, call: (Table) -> Unit = {}): List<Any> {
        val tables = mutableListOf<Any>()
        var remarks = ""
        var primaryKeyNames = mutableListOf<String>()
        var indexes = mutableListOf<Indexed>()
        var pumlColumns = mutableListOf<Any>()
        var tableName = ""
        var isField = false
        var isUml = false
        var moduleName: String? = null
        puml.readLines().forEach {
            if (it.isNotBlank()) {
                val line = it.trim()
                if (line.startsWith("@startuml")) {
                    moduleName = line.substringAfter("@startuml").trim()
                    isUml = true
                } else if (line.startsWith("entity ")) {
                    val fieldDef = line.split(" ")
                    tableName = fieldDef[1].trim()
                } else if (tableName.isNotBlank() && !isField) {
                    if ("==" == line)
                        isField = true
                    else
                        remarks = line
                } else if (isField) {
                    val uniqueMult = line.startsWith("'UNIQUE")
                    if (uniqueMult || line.startsWith("'INDEX")) {
                        val columnNames =
                            line.substringAfter(if (uniqueMult) "'UNIQUE" else "'INDEX").trim()
                        indexes.add(
                            Indexed(
                                "${if (uniqueMult) "UK" else "IDX"}_${
                                    tableName.replace(
                                        "_",
                                        ""
                                    ).takeLast(7)
                                }_${
                                    columnNames.replace(tableName, "").replace("_", "")
                                        .replace(",", "").takeLast(7)
                                }", uniqueMult, columnNames.split(",").toMutableList()
                            )
                        )
                    } else if ("}" == line) {
                        val table = Table(
                            productName = DataType.PUML.name,
                            catalog = null,
                            schema = null,
                            tableName = tableName,
                            tableType = "",
                            remarks = remarks,
                            primaryKeyNames = primaryKeyNames,
                            indexes = indexes,
                            pumlColumns = pumlColumns,
                            moduleName = moduleName ?: "database"
                        )
                        call(table)
                        tables.add(table)

                        primaryKeyNames = mutableListOf()
                        indexes = mutableListOf()
                        pumlColumns = mutableListOf()
                        tableName = ""
                        remarks = ""
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
                        val unsigned = columnDef.contains("UNSIGNED", true)
                        //DEFAULT
                        var defaultVal: String? = null
                        if (columnDef.contains("DEFAULT")) {
                            defaultVal =
                                columnDef.substringAfter("DEFAULT").trim().substringBefore(" ")
                                    .trim('\'').trim()
                            extra = extra.replace(Regex(" DEFAULT +'?$defaultVal'?"), "")
                        }
                        // SEQUENCE
                        var sequence = ""
                        var sequenceStartWith = 1
                        if (columnDef.contains("SEQUENCE")) {
                            sequence =
                                columnDef.substringAfter("SEQUENCE").trim().substringBefore(" ")
                                    .trim()
                            val regex = Regex(".* SEQUENCE +$sequence +(\\d+) .*")
                            if (columnDef.matches(regex)) {
                                sequenceStartWith = columnDef.replace(regex, "$1").toInt()
                                extra = extra.replace(Regex(" SEQUENCE +$sequence +(\\d+)"), "")
                            } else {
                                extra = extra.replace(Regex(" SEQUENCE +$sequence"), "")
                            }
                        }
                        var fk = false
                        var refTable: String? = null
                        var refColumn: String? = null
                        if (columnDef.contains("FK")) {//FK > docs.id
                            val ref =
                                columnDef.substringAfter("FK >").trim().substringBefore(" ").trim()
                            extra = extra.replace(Regex(" FK > +$ref"), "")
                            val refs = ref.split(".")
                            fk = true
                            refTable = refs[0]
                            refColumn = refs[1]
                        }
                        val typeName = type.substringBefore("(")
                        val unique = columnDef.contains("UNIQUE", true)
                        val indexed = columnDef.contains("INDEX", true)
                        val autoIncrement = columnDef.contains(
                            "AUTO_INCREMENT",
                            true
                        ) || columnDef.contains("AUTOINCREMENT", true)
                        extra = extra.replace(" UNSIGNED", "", true)
                        extra = extra.replace(" UNIQUE", "", true)
                        extra = extra.replace(" INDEX", "", true)
                        extra = extra.replace(" AUTO_INCREMENT", "", true)
                        extra = extra.replace(" AUTOINCREMENT", "", true)
                        extra = extra.replace(" NOT NULL", "", true)
                        extra = extra.replace(" NULL", "", true)
                        extra = extra.replace(" PK", "", true)
                        extra = extra.replace(" IDGENERATOR", "", true)
                        extra = extra.replace(" VERSION", "", true).trim()
                        val column = Column(
                            tableCat = null,
                            columnName = columnName,
                            remarks = lineDef.last().trim(),
                            typeName = typeName,
                            dataType = JavaTypeResolver.calculateDataType(typeName),
                            columnSize = columnSize,
                            decimalDigits = decimalDigits,
                            nullable = !columnDef.contains("NOT NULL", true),
                            unique = unique,
                            indexed = indexed,
                            columnDef = defaultVal,
                            extra = extra,
                            tableSchem = null,
                            isForeignKey = fk,
                            unsigned = unsigned,
                            pktableName = refTable,
                            pkcolumnName = refColumn,
                            autoIncrement = autoIncrement,
                            idgenerator = columnDef.contains(
                                "IDGENERATOR",
                                true
                            ),
                            version = columnDef.contains(
                                "VERSION",
                                true
                            ),
                            sequence = sequence,
                            sequenceStartWith = sequenceStartWith
                        )
                        if (unique)
                            indexes.add(
                                Indexed(
                                    "UK_${
                                        tableName.replace("_", "").takeLast(7)
                                    }_${
                                        columnName.replace(tableName, "").replace("_", "")
                                            .replace(",", "").takeLast(7)
                                    }", true, mutableListOf(columnName)
                                )
                            )
                        if (indexed)
                            indexes.add(
                                Indexed(
                                    "IDX_${
                                        tableName.replace("_", "").takeLast(7)
                                    }_${
                                        columnName.replace(tableName, "").replace("_", "")
                                            .replace(",", "").takeLast(7)
                                    }", false, mutableListOf(columnName)
                                )
                            )
                        if (columnDef.contains("PK", true)) {
                            primaryKeyNames.add(column.columnName)
                        }
                        pumlColumns.add(column)
                    } else {
                        pumlColumns.add(line)
                    }
                } else if (line.startsWith("@enduml")) {
                    isUml = false
                } else if (isUml && line.isNotBlank() && !line.matches(Regex("^.* \\|\\|--o\\{ .*$"))) {
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

    private fun compile(
        extension: GeneratorExtension,
        tables: List<Any>,
        out: File,
        remarksProperties: Properties? = null
    ) {
        if (tables.isNotEmpty()) {
            val any = tables[0]
            val plantUML = PlantUML(
                if (any is Table) any.moduleName else null,
                out.absolutePath,
                if ("database.puml" == out.name) remarksProperties else null
            )
            plantUML.setUp(extension)
            tables.forEach {
                if (it is Table) {
                    plantUML.run(it)
                } else {
                    plantUML.appendlnText(it.toString())
                }
            }
            plantUML.tearDown()
        }
    }

    fun reformat(extension: GeneratorExtension) {
        val remarksProperties = Properties()
        val remarksFile = extension.file("puml/remarks.properties")
        if (remarksFile.exists()) {
            remarksProperties.load(remarksFile.inputStream())
        }
        (extension.pumlSrcSources + extension.file(extension.pumlDatabase + "/database.puml")).forEach {
            if (it.exists()) {
                when (extension.pumlDatabaseDriver) {
                    top.bettercode.generator.DatabaseDriver.MYSQL -> toMysql(
                        extension,
                        it,
                        it,
                        remarksProperties
                    )
                    top.bettercode.generator.DatabaseDriver.ORACLE -> toOracle(
                        extension,
                        it,
                        it,
                        remarksProperties
                    )
                    else -> compile(extension, it, it, remarksProperties)
                }
            }
        }
    }

    fun compile(
        extension: GeneratorExtension,
        src: File,
        out: File,
        remarksProperties: Properties? = null
    ) {
        compile(extension, toTableOrAnys(src), out, remarksProperties)
    }

    fun toMysql(
        extension: GeneratorExtension, src: File, out: File,
        remarksProperties: Properties? = null
    ) {
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
                                        in 1..4 -> {
                                            it.typeName = "TINYINT"
                                        }
                                        in 5..6 -> {
                                            it.typeName = "SMALLINT"
                                        }
                                        in 7..9 -> {
                                            it.typeName = "MEDUIMINT"
                                        }
                                        in 10..11 -> {
                                            it.typeName = "INT"
                                        }
                                        in 12..20 -> {
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
        compile(extension, tables, out, remarksProperties)
    }

    fun toOracle(
        extension: GeneratorExtension, src: File, out: File,
        remarksProperties: Properties? = null
    ) {
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
        compile(extension, tables, out, remarksProperties)
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
                        top.bettercode.generator.DatabaseDriver.MYSQL -> MysqlToDDL.toDDL(
                            toTables(it),
                            extension.pumlSqlOutputFile(it, extension.file(extension.pumlSrc))
                        )
                        top.bettercode.generator.DatabaseDriver.ORACLE -> OracleToDDL.toDDL(
                            toTables(it),
                            extension.pumlSqlOutputFile(it, extension.file(extension.pumlSrc))
                        )
                        top.bettercode.generator.DatabaseDriver.SQLITE -> SqlLiteToDDL.toDDL(
                            toTables(it),
                            extension.pumlSqlOutputFile(it, extension.file(extension.pumlSrc))
                        )
                        else -> {
                            throw IllegalArgumentException("不支持的数据库")
                        }
                    }
                }
            }
            DataType.PDM -> {
                val pdmFile = extension.file(extension.pdmSrc)
                when (extension.pumlDatabaseDriver) {
                    top.bettercode.generator.DatabaseDriver.MYSQL -> MysqlToDDL.toDDL(
                        PdmReader.read(pdmFile),
                        extension.pumlSqlOutputFile(pdmFile, pdmFile.parentFile)
                    )
                    top.bettercode.generator.DatabaseDriver.ORACLE -> OracleToDDL.toDDL(
                        PdmReader.read(pdmFile),
                        extension.pumlSqlOutputFile(pdmFile, pdmFile.parentFile)
                    )
                    top.bettercode.generator.DatabaseDriver.SQLITE -> SqlLiteToDDL.toDDL(
                        PdmReader.read(pdmFile),
                        extension.pumlSqlOutputFile(pdmFile, pdmFile.parentFile)
                    )
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
            val deleteTablesWhenUpdate = extension.dropTablesWhenUpdate
            val databaseFile = extension.file(extension.pumlDatabase + "/database.puml")

            val allTables = mutableListOf<Table>()
            when (extension.dataType) {
                DataType.PUML -> {
                    extension.pumlSrcSources.forEach { file ->
                        val tables = toTables(file)
                        allTables.addAll(tables)
                        when (extension.pumlDatabaseDriver) {
                            top.bettercode.generator.DatabaseDriver.MYSQL -> MysqlToDDL.toDDLUpdate(
                                if (deleteTablesWhenUpdate) oldTables(
                                    extension,
                                    databaseFile
                                ) else oldTables(
                                    extension,
                                    databaseFile,
                                    tables.map { it.tableName }.toTypedArray()
                                ), tables, pw, extension
                            )
                            top.bettercode.generator.DatabaseDriver.ORACLE -> OracleToDDL.toDDLUpdate(
                                if (deleteTablesWhenUpdate) oldTables(
                                    extension,
                                    databaseFile
                                ) else oldTables(
                                    extension,
                                    databaseFile,
                                    tables.map { it.tableName }.toTypedArray()
                                ), tables, pw, extension
                            )
                            top.bettercode.generator.DatabaseDriver.SQLITE -> SqlLiteToDDL.toDDLUpdate(
                                if (deleteTablesWhenUpdate) oldTables(
                                    extension,
                                    databaseFile
                                ) else oldTables(
                                    extension,
                                    databaseFile,
                                    tables.map { it.tableName }.toTypedArray()
                                ), tables, pw, extension
                            )
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
                        top.bettercode.generator.DatabaseDriver.MYSQL -> MysqlToDDL.toDDLUpdate(
                            if (deleteTablesWhenUpdate) oldTables(
                                extension,
                                databaseFile
                            ) else oldTables(
                                extension,
                                databaseFile,
                                tables.map { it.tableName }.toTypedArray()
                            ), tables, pw, extension
                        )
                        top.bettercode.generator.DatabaseDriver.ORACLE -> OracleToDDL.toDDLUpdate(
                            if (deleteTablesWhenUpdate) oldTables(
                                extension,
                                databaseFile
                            ) else oldTables(
                                extension,
                                databaseFile,
                                tables.map { it.tableName }.toTypedArray()
                            ), tables, pw, extension
                        )
                        top.bettercode.generator.DatabaseDriver.SQLITE -> SqlLiteToDDL.toDDLUpdate(
                            if (deleteTablesWhenUpdate) oldTables(
                                extension,
                                databaseFile
                            ) else oldTables(
                                extension,
                                databaseFile,
                                tables.map { it.tableName }.toTypedArray()
                            ), tables, pw, extension
                        )
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

    private fun oldTables(
        extension: GeneratorExtension,
        databaseFile: File,
        tableNameList: Array<String>? = null
    ): List<Table> {
        return if (DataType.PUML == extension.updateFromType && databaseFile.exists()) {
            toTables(databaseFile)
        } else {
            extension.use { extension.tables(tableNameList ?: tableNames().toTypedArray()) }
        }
    }

}
