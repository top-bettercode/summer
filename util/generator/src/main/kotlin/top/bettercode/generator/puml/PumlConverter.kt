package top.bettercode.generator.puml

import top.bettercode.generator.DataType
import top.bettercode.generator.DatabaseDriver
import top.bettercode.generator.GeneratorExtension
import top.bettercode.generator.database.entity.Column
import top.bettercode.generator.database.entity.Indexed
import top.bettercode.generator.database.entity.Table
import top.bettercode.generator.dom.java.JavaTypeResolver
import top.bettercode.generator.dsl.def.PlantUML
import java.io.File
import java.util.*

/**
 * @author Peter Wu
 * @since 0.0.45
 */
object PumlConverter {

    fun toTables(
        puml: File,
        call: (Table) -> Unit = {}
    ): List<Table> {
        return toTableOrAnys(puml, call).filterIsInstance<Table>().sortedBy { it.tableName }
            .toList()
    }

    private fun toTableOrAnys(
        pumlFile: File,
        call: (Table) -> Unit = {}
    ): List<Any> {
        val tables = mutableListOf<Any>()
        var remarks = ""
        var primaryKeyNames = mutableListOf<String>()
        var indexes = mutableListOf<Indexed>()
        var pumlColumns = mutableListOf<Any>()
        var tableName = ""
        var isField = false
        var isUml = false
        var subModuleName: String? = null
        pumlFile.readLines().forEach {
            if (it.isNotBlank()) {
                val line = it.trim()
                if (line.startsWith("@startuml")) {
                    subModuleName = line.substringAfter("@startuml").trim()
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
                            subModule = pumlFile.nameWithoutExtension,
                            subModuleName = subModuleName ?: "database"
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
                                    .trim('\'')
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
                        extra = extra.replace(" [A-Z0-9]*IDGENERATOR\\d*".toRegex(), "")
                        extra = extra.replace(" VERSION", "", true)
                        extra = extra.replace(" SOFTDELETE", "", true)
                        extra = extra.replace(" ASBOOLEAN", "", true)
                        val idgeneratorRegex = ".* ([A-Z0-9]*IDGENERATOR\\d*) .*".toRegex()
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
                            extra = extra.trim(),
                            tableSchem = null,
                            isForeignKey = fk,
                            unsigned = unsigned,
                            pktableName = refTable,
                            pkcolumnName = refColumn,
                            autoIncrement = autoIncrement,
                            idgenerator = if (columnDef.matches(idgeneratorRegex)) columnDef.replace(
                                idgeneratorRegex,
                                "$1"
                            ) else "",
                            version = columnDef.contains(
                                "VERSION",
                                true
                            ),
                            softDelete = columnDef.contains(
                                "SOFTDELETE",
                                true
                            ),
                            asBoolean = columnDef.contains(
                                "ASBOOLEAN",
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

    fun compile(
        extension: GeneratorExtension,
        tables: List<Any>,
        out: File,
        remarksProperties: Properties? = null
    ) {
        if (tables.isNotEmpty()) {
            val any = tables[0]
            val plantUML = PlantUML(
                if (any is Table) any.subModuleName else null,
                out,
                if ("database" == out.parent) remarksProperties else null
            )
            plantUML.setUp(extension)
            tables.distinctBy { if (it is Table) it.tableName else it }.forEach {
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
        (extension.pumlSources + extension.pumlDatabaseSources).forEach { (module, files) ->
            files.forEach { file ->
                val driver = extension.datasources[module]
                    ?.databaseDriver ?: DatabaseDriver.UNKNOWN
                when (driver) {
                    DatabaseDriver.MYSQL -> toMysql(
                        extension,
                        module,
                        file,
                        file,
                        remarksProperties
                    )
                    DatabaseDriver.ORACLE -> toOracle(
                        extension,
                        module,
                        file,
                        file,
                        remarksProperties
                    )
                    else -> compile(extension, module, file, file, remarksProperties)
                }
            }

        }
    }

    fun compile(
        extension: GeneratorExtension,
        module: String,
        src: File,
        out: File,
        remarksProperties: Properties? = null
    ) {
        compile(
            extension,
            toTableOrAnys(src) {
                it.ext = extension
                it.module = module
                it.datasource = extension.datasources[module]
            },
            out,
            remarksProperties
        )
    }

    fun toMysql(
        extension: GeneratorExtension,
        module: String,
        src: File, out: File,
        remarksProperties: Properties? = null
    ) {
        val tables = toTableOrAnys(src) {
            it.ext = extension
            it.module = module
            it.datasource = extension.datasources[module]
        }
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
        extension: GeneratorExtension,
        module: String,
        src: File, out: File,
        remarksProperties: Properties? = null
    ) {
        val tables = toTableOrAnys(src) {
            it.ext = extension
            it.module = module
            it.datasource = extension.datasources[module]
        }
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


}
