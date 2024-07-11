package top.bettercode.summer.tools.generator.puml

import org.springframework.util.Assert
import top.bettercode.summer.tools.generator.DatabaseConfiguration
import top.bettercode.summer.tools.generator.DatabaseDriver
import top.bettercode.summer.tools.generator.GeneratorExtension
import top.bettercode.summer.tools.generator.PumlTableHolder
import top.bettercode.summer.tools.generator.database.entity.Column
import top.bettercode.summer.tools.generator.database.entity.Indexed
import top.bettercode.summer.tools.generator.database.entity.Table
import top.bettercode.summer.tools.generator.dsl.def.PlantUML
import top.bettercode.summer.tools.lang.decapitalized
import top.bettercode.summer.tools.lang.util.JavaTypeResolver
import java.io.File
import java.util.*

/**
 * @author Peter Wu
 * @since 0.0.45
 */
object PumlConverter {

    private val codeTypeCache = mutableMapOf<String, String?>()

    fun toTables(
        database: DatabaseConfiguration,
        puml: File
    ): List<Table> {
        return toTableOrAnys(database, puml).filterIsInstance<Table>()
    }

    private fun toTableOrAnys(
        database: DatabaseConfiguration,
        pumlFile: File
    ): List<Any> {
        val tables = mutableListOf<Any>()
        var tableRemarks = ""
        var primaryKeyNames = mutableListOf<String>()
        var indexes = mutableListOf<Indexed>()
        var pumlColumns = mutableListOf<Any>()
        var tableName = ""
        var isField = false
        var isUml = false
        var engine: String? = null
        var subModuleName: String? = null
        pumlFile.readLines().forEach { l ->
            if (l.isNotBlank()) {
                val line = l.trim()
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
                        tableRemarks = line
                } else if (isField) {
                    val uniqueMult = line.startsWith("'UNIQUE")
                    if (uniqueMult || line.startsWith("'INDEX")) {
                        val columnNames =
                            line.substringAfter(if (uniqueMult) "'UNIQUE" else "'INDEX").trim()
                        indexes.add(
                            Indexed(
                                unique = uniqueMult,
                                columnName = columnNames.split(",").toMutableList()
                            )
                        )
                    } else if (line.startsWith("'ENGINE")) {
                        engine = line.substringAfter("=").trim()
                    } else if ("}" == line) {
                        val table = Table(
                            productName = top.bettercode.summer.tools.generator.DataType.PUML.name,
                            catalog = null,
                            schema = null,
                            tableName = tableName,
                            tableType = "",
                            remarks = tableRemarks,
                            primaryKeyNames = primaryKeyNames,
                            indexes = indexes,
                            pumlColumns = pumlColumns,
                            subModule = pumlFile.nameWithoutExtension,
                            subModuleName = subModuleName ?: "database",
                            engine = engine ?: "InnoDB"
                        )
                        table.file = pumlFile
                        table.database = database
                        table.collate = database.collate
                        tables.add(table)

                        primaryKeyNames = mutableListOf()
                        indexes = mutableListOf()
                        pumlColumns = mutableListOf()
                        tableName = ""
                        tableRemarks = ""
                        engine = null
                        isField = false
                    } else if (!line.startsWith("'")) {
                        val lineDef = line.trim().replace(" +".toRegex(), " ").split("--")
                        val remarks = lineDef.last().trim()
                        val fieldDef = lineDef[0]
                        val fieldDefs = fieldDef.split(" +: +".toRegex())
                        val columnName = fieldDefs[0].trim()
                        Assert.isTrue(fieldDefs.size == 2, "字段定义错误：$line")
                        val columnDef = fieldDefs[1]
                        val unsigned = columnDef.contains(" UNSIGNED ", true)
                        val unique = columnDef.contains(" UNIQUE ", true)
                        val indexed = columnDef.contains(" INDEX ", true)
                        val autoIncrement = columnDef.contains(
                            " AUTO_INCREMENT ",
                            true
                        ) || columnDef.contains(" AUTOINCREMENT ", true)
                        val version = columnDef.contains(" VERSION ", true)
                        var createdDate = columnDef.contains(" CREATEDDATE ", true)
                        val createdBy = columnDef.contains(" CREATEDBY ", true)
                        var lastModifiedDate = columnDef.contains(" LASTMODIFIEDDATE ", true)
                        val lastModifiedBy = columnDef.contains(" LASTMODIFIEDBY ", true)
                        var logicalDelete = columnDef.contains(" LOGICALDELETE ", true)
                        //兼容
                        if (!logicalDelete && columnDef.contains(" SOFTDELETE ", true)) {
                            logicalDelete = true
                        }
                        val pk = columnDef.contains(" PK ", true)
                        val nullable =
                            if (pk || version || logicalDelete) false else !columnDef.contains(
                                " NOT NULL ",
                                true
                            )
                        val asBoolean = columnDef.contains(" ASBOOLEAN ", true)

                        val type = columnDef.split(" ")[0].trim()
                        val typeName = type.substringBefore("(")
                        val (columnSize, decimalDigits) = parseType(type)

                        var extra = columnDef.substringAfter(type)
                        extra = extra.replace(" UNSIGNED ", " ", true)
                        extra = extra.replace(" UNIQUE ", " ", true)
                        extra = extra.replace(" INDEX ", " ", true)
                        extra = extra.replace(" AUTO_INCREMENT ", " ", true)
                        extra = extra.replace(" AUTOINCREMENT ", " ", true)
                        extra = extra.replace(" NOT NULL ", " ", true)
                        extra = extra.replace(" NULL ", " ", true)
                        extra = extra.replace(" PK ", " ", true)
                        extra = extra.replace(" VERSION ", " ", true)
                        extra = extra.replace(" CREATEDDATE ", " ", true)
                        extra = extra.replace(" CREATEDBY ", " ", true)
                        extra = extra.replace(" LASTMODIFIEDDATE ", " ", true)
                        extra = extra.replace(" LASTMODIFIEDBY ", " ", true)
                        extra = extra.replace(" LOGICALDELETE ", " ", true)
                        extra = extra.replace(" ASBOOLEAN ", " ", true)
                        //兼容
                        extra = extra.replace(" SOFTDELETE ", " ", true)

                        //DEFAULT
                        var defaultVal: String? = null
                        if (extra.contains(" DEFAULT ")) {
                            val defaultRawVal =
                                extra.substringAfter(" DEFAULT ").substringBefore(" ")
                            defaultVal = defaultRawVal.trim()
                            extra = extra.replace(" DEFAULT $defaultRawVal ", " ", true)
                        }

                        //FK
                        var fk = false
                        var refTable: String? = null
                        var refColumn: String? = null
                        if (extra.contains(" FK > ")) {//FK > docs.id
                            val ref = extra.substringAfter(" FK > ").trim()
                                .substringBefore(" ").trim()
                            extra = extra.replace(" FK > $ref", " ", true)
                            val refs = ref.split(".")
                            fk = true
                            refTable = refs[0]
                            refColumn = refs[1]
                        }

                        // SEQUENCE
                        var sequence = ""
                        var sequenceStartWith = 1L
                        val sequenceRegex = Regex(" SEQUENCE +(.+?)(\\(\\d+\\))? ")
                        val sequenceMatch = sequenceRegex.find(extra)
                        if (sequenceMatch != null) {
                            val groupValues = sequenceMatch.groupValues
                            sequence = groupValues[1]
                            sequenceStartWith = groupValues[2].trim('(', ')').toLongOrNull() ?: 1L
                            extra = extra.replace(sequenceRegex, " ")
                        }

                        //CODETYPE
                        var codeType = ""
                        val codeTypeRegex = Regex(" CODETYPE +(.+) ")
                        val codeTypeMatch = codeTypeRegex.find(extra)
                        if (codeTypeMatch != null) {
                            codeType = codeTypeMatch.groupValues[1]
                            extra = extra.replace(codeTypeRegex, " ")
                        }

                        val isCodeType =
                            !asBoolean && remarks.matches(Regex(".*\\((.*:.*[; ]?)+\\).*"))
                        if (isCodeType) {
                            if (codeType.isBlank()) {
                                codeType =
                                    if (columnName.contains("_") || logicalDelete || database.extension.commonCodeTypes.any {
                                            it.equals(
                                                columnName,
                                                true
                                            )
                                        })
                                        GeneratorExtension.javaName(columnName)
                                    else
                                        database.className(tableName)
                                            .decapitalized() + GeneratorExtension.javaName(
                                            columnName,
                                            true
                                        )
                            }

                            val exist = codeTypeCache[codeType]
                            if (codeTypeCache.containsKey(codeType) && remarks != exist) {
                                throw IllegalArgumentException("${pumlFile.name}:$tableName:$columnName codeType重复：$codeType,[$remarks]!=[$exist]")
                            } else {
                                codeTypeCache[codeType] = remarks
                            }
                        } else {
                            codeType = ""
                        }

                        //IDGENERATOR
                        var idgenerator = ""
                        var idgeneratorParam = ""
                        val idRegex = Regex(" ([A-Z0-9]*IDGENERATOR\\d*)(\\(.*\\))? ")
                        val idgeneratorMatch = idRegex.find(extra)
                        if (idgeneratorMatch != null) {
                            idgenerator = idgeneratorMatch.groupValues[1]
                            idgeneratorParam = idgeneratorMatch.groupValues[2].trim('(', ')')
                            extra = extra.replace(idRegex, " ")
                        }

                        //兼容
                        if (!createdDate && columnName.equals(
                                "created_date",
                                true
                            ) && defaultVal.isNullOrBlank()
                        ) {
                            createdDate = true
                        }
                        if (!lastModifiedDate && columnName.equals(
                                "last_modified_date",
                                true
                            ) && !extra.contains("ON UPDATE CURRENT_TIMESTAMP", true)
                        ) {
                            lastModifiedDate = true
                        }

                        val column = Column(
                            tableCat = null,
                            columnName = columnName,
                            remarks = remarks,
                            typeName = typeName,
                            dataType = JavaTypeResolver.calculateDataType(typeName),
                            columnSize = columnSize,
                            decimalDigits = decimalDigits,
                            nullable = nullable,
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
                            idgenerator = idgenerator.trim(),
                            idgeneratorParam = idgeneratorParam.trim(),
                            version = version,
                            createdDate = createdDate,
                            createdBy = createdBy,
                            lastModifiedDate = lastModifiedDate,
                            lastModifiedBy = lastModifiedBy,
                            logicalDelete = logicalDelete,
                            asBoolean = asBoolean,
                            sequence = sequence.trim(),
                            sequenceStartWith = sequenceStartWith,
                            codeType = codeType
                        )
                        if (unique)
                            indexes.add(
                                Indexed(
                                    unique = true,
                                    columnName = mutableListOf(columnName)
                                )
                            )
                        if (indexed)
                            indexes.add(
                                Indexed(
                                    unique = false,
                                    columnName = mutableListOf(columnName)
                                )
                            )
                        if (pk) {
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
        database: DatabaseConfiguration,
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
            plantUML.setUp(database.extension)
            tables.distinctBy { if (it is Table) it.tableName else it }.forEach {
                if (it is Table) {
                    plantUML.run(it)
                } else {
                    plantUML.appendLineText(it.toString())
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
                val database = extension.database(module)
                when (database.driver) {
                    DatabaseDriver.MYSQL -> toMysql(
                        database,
                        file,
                        file,
                        remarksProperties
                    )

                    DatabaseDriver.ORACLE -> toOracle(
                        database,
                        file,
                        file,
                        remarksProperties
                    )

                    else -> compile(database, file, file, remarksProperties)
                }
            }

        }
    }

    fun toDatabase(extension: GeneratorExtension) {
        val remarksProperties = Properties()
        val remarksFile = extension.file("puml/remarks.properties")
        if (remarksFile.exists()) {
            remarksProperties.load(remarksFile.inputStream())
        }
        (extension.pumlSources + extension.pumlDatabaseSources).forEach { (module, files) ->
            val database = extension.database(module)
            val tables = PumlTableHolder(database, files).tables(false)
            compile(
                database,
                tables,
                extension.file(extension.pumlSrc + "/database/$module.puml"),
                remarksProperties
            )
        }
    }

    fun compile(
        database: DatabaseConfiguration,
        src: File,
        out: File,
        remarksProperties: Properties? = null
    ) {
        codeTypeCache.clear()
        compile(
            database,
            toTableOrAnys(database, src),
            out,
            remarksProperties
        )
    }

    fun toMysql(
        database: DatabaseConfiguration,
        src: File, out: File,
        remarksProperties: Properties? = null
    ) {
        val tables = toTableOrAnys(database, src)
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
        compile(database, tables, out, remarksProperties)
    }

    fun toOracle(
        database: DatabaseConfiguration,
        src: File, out: File,
        remarksProperties: Properties? = null
    ) {
        val tables = toTableOrAnys(database, src)
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
        compile(database, tables, out, remarksProperties)
    }

    fun cleanCache() {
        codeTypeCache.clear()
    }


}
