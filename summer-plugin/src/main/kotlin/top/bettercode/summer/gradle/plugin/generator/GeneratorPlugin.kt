package top.bettercode.summer.gradle.plugin.generator

import isBoot
import isCore
import net.sourceforge.plantuml.FileFormat
import net.sourceforge.plantuml.FileFormatOption
import net.sourceforge.plantuml.ISourceFileReader
import net.sourceforge.plantuml.SourceFileReader
import notEmptyDir
import org.gradle.api.Action
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.Task
import top.bettercode.summer.gradle.plugin.profile.ProfileExtension.Companion.profilesActive
import top.bettercode.summer.tools.generator.DatabaseConfiguration
import top.bettercode.summer.tools.generator.DatabaseDriver
import top.bettercode.summer.tools.generator.GeneratorExtension
import top.bettercode.summer.tools.generator.GeneratorExtension.Companion.DEFAULT_MODULE_NAME
import top.bettercode.summer.tools.generator.PumlTableHolder
import top.bettercode.summer.tools.generator.database.entity.Column
import top.bettercode.summer.tools.generator.database.entity.Table
import top.bettercode.summer.tools.generator.ddl.MysqlToDDL
import top.bettercode.summer.tools.generator.ddl.OracleToDDL
import top.bettercode.summer.tools.generator.ddl.SqlLiteToDDL
import top.bettercode.summer.tools.generator.dom.unit.FileUnit
import top.bettercode.summer.tools.generator.dsl.Generator
import top.bettercode.summer.tools.generator.dsl.def.PlantUML
import top.bettercode.summer.tools.generator.puml.PumlConverter
import top.bettercode.summer.tools.lang.capitalized
import java.io.File
import java.util.*


/**
 *
 * 注册task
 *
 * @author Peter Wu
 */
@Suppress("ObjectLiteralToLambda")
class GeneratorPlugin : Plugin<Project> {
    companion object {
        const val GEN_GROUP = "gen"
        const val PRINT_GROUP = "print"
        const val PUML_GROUP = "puml"
    }

    override fun apply(project: Project) {
        project.extensions.create("generator", GeneratorExtension::class.java)
        project.extensions.configure(GeneratorExtension::class.java) { extension ->

            val entries = project.properties.filter { it.key.startsWith("datasource.") }.entries
            extension.databases =
                ((if (project.properties.containsKey("datasource.url")) mapOf(DEFAULT_MODULE_NAME to (entries.filter {
                    it.key.split('.').size == 2
                }.associateBy({ it.key.substringAfter("datasource.") }, { it.value }))
                ) else emptyMap()) + entries.filter { it.key.split('.').size == 3 }.groupBy {
                    it.key.substringAfter("datasource.").substringBefore(".")
                }.mapValues { entry ->
                    entry.value.groupBy({ it.key.substringAfter("datasource.${entry.key}.") },
                        { it.value }).mapValues { it.value.last() }
                }).mapValues { (module, properties) ->
                    val database = DatabaseConfiguration()
                    database.module = module
                    database.url = (properties["url"] as String?) ?: ""
                    database.driver =
                        DatabaseDriver.fromProductName(
                            (properties["productName"] as String?)
                                ?: ""
                        )
                    database.catalog = properties["catalog"] as String?
                    database.schema = properties["schema"] as String?
                    database.username = properties["username"] as String? ?: "root"
                    database.password = properties["password"] as String? ?: "root"
                    database.driverClass = properties["driverClass"] as String? ?: ""

                    database.sshHost = properties["sshHost"] as String?
                    database.sshPort = (properties["sshPort"] as String?)?.toInt() ?: 22
                    database.sshUsername = properties["sshUsername"] as String?
                    database.sshPassword = properties["sshPassword"] as String?

                    database.debug = (properties["debug"] as String? ?: "false").toBoolean()
                    database.offline = (properties["offline"] as String? ?: "false").toBoolean()
                    database.queryIndex = (properties["queryIndex"]
                        ?: findGeneratorProperty(project, "queryIndex"))?.toString()?.toBoolean()
                        ?: true

                    database.tinyInt1isBit =
                        (properties["tinyInt1isBit"] as String? ?: "false").toBoolean()
                    if (database.isOracle) {
                        database.properties["oracle.net.CONNECT_TIMEOUT"] = "10000"
                    }

                    database.entityPrefix =
                        properties["entityPrefix"] as String? ?: findGeneratorProperty(
                            project,
                            "entityPrefix"
                        ) ?: ""

                    database.tablePrefixes =
                        (properties["tablePrefix"] as String? ?: findGeneratorProperty(
                            project,
                            "tablePrefix"
                        ) ?: "").split(",")
                            .filter { it.isNotBlank() }.toTypedArray()

                    database.tableSuffixes =
                        (properties["tableSuffix"] as String? ?: findGeneratorProperty(
                            project,
                            "tableSuffix"
                        ) ?: "").split(",")
                            .filter { it.isNotBlank() }.toTypedArray()

                    database.dropTablesWhenUpdate =
                        (properties["dropTablesWhenUpdate"]
                            ?: findGeneratorProperty(project, "dropTablesWhenUpdate"))?.toString()
                            ?.toBoolean()
                            ?: false
                    database.dropColumnsWhenUpdate =
                        (properties["dropColumnsWhenUpdate"]
                            ?: findGeneratorProperty(project, "dropColumnsWhenUpdate"))?.toString()
                            ?.toBoolean()
                            ?: false

                    database.includeSchema = (properties["include-schema"]
                        ?: findGeneratorProperty(project, "include-schema"))?.toString()
                        ?.toBoolean()
                        ?: true

                    database.storeToken = (properties["storeToken"]
                        ?: findGeneratorProperty(project, "storeToken"))?.toString()?.toBoolean()
                        ?: database.isDefault

                    database.collate = (properties["collate"]
                        ?: findGeneratorProperty(project, "collate"))?.toString()

                    database.excludeTableNames = (properties["excludeTableNames"]
                        ?: findGeneratorProperty(project, "excludeTableNames")
                        ?: "").toString().split(",").asSequence().filter { it.isNotBlank() }
                        .map { it.trim() }
                        .distinct()
                        .sortedBy { it }.toList()
                        .toTypedArray()

                    database.excludeGenTableNames = (properties["excludeGenTableNames"]
                        ?: findGeneratorProperty(project, "excludeGenTableNames")
                        ?: "api_token").toString().split(",").asSequence()
                        .filter { it.isNotBlank() }.map { it.trim() }
                        .distinct()
                        .sortedBy { it }.toList()
                        .toTypedArray()

                    database
                }.toSortedMap(GeneratorExtension.comparator)

            extension.delete = (findGeneratorProperty(project, "delete"))?.toBoolean() ?: false
            extension.projectPackage =
                (findGeneratorProperty(project, "project-package"))?.toBoolean()
                    ?: false
            extension.useJSR310Types =
                (findGeneratorProperty(project, "useJSR310Types"))?.toBoolean() ?: true
            extension.forceIntegers =
                (findGeneratorProperty(project, "forceIntegers"))?.toBoolean() ?: true
            extension.forceBigDecimals =
                (findGeneratorProperty(project, "forceBigDecimals"))?.toBoolean() ?: false

            extension.replaceAll =
                (findGeneratorProperty(project, "replaceAll"))?.toBoolean() ?: false
            extension.useForeignKey =
                (findGeneratorProperty(project, "useForeignKey"))?.toBoolean() ?: false
            extension.sqlQuote = (findGeneratorProperty(project, "sqlQuote"))?.toBoolean() ?: true
            extension.rootPath = project.rootDir
            extension.projectDir = project.projectDir
            extension.dir = findGeneratorProperty(project, "dir") ?: "src/main/java"
            extension.packageName =
                (findGeneratorProperty(project, "packageName")
                    ?: project.findProperty("app.packageName") as String?
                    ?: "")
            extension.userModule =
                (findGeneratorProperty(project, "userModule"))?.toBoolean() ?: true
            extension.applicationName = project.findProperty("application.name") as String?
                ?: project.rootProject.name
            extension.projectName =
                (findGeneratorProperty(project, "projectName") ?: project.name)
            extension.isCore = project.isCore

            extension.primaryKeyName = findGeneratorProperty(project, "primaryKeyName") ?: "id"
            extension.remarks = findGeneratorProperty(project, "remarks") ?: ""
            extension.commonCodeTypes = (findGeneratorProperty(project, "commonCodeTypes")
                ?: extension.logicalDeleteColumnName).split(",").asSequence()
                .filter { it.isNotBlank() }.map { it.trim() }.toList()
                .toTypedArray()
            extension.logicalDeleteColumnName =
                findGeneratorProperty(project, "logicalDeleteColumnName")
                    ?: "deleted"
            extension.logicalDeleteAsBoolean =
                (findGeneratorProperty(project, "logicalDeleteAsBoolean"))?.toBoolean()
                    ?: true
            //puml
            extension.pumlSrc = findGeneratorProperty(project, "puml.src") ?: "puml"
            extension.pumlDiagramFormat =
                findGeneratorProperty(project, "puml.diagramFormat") ?: "PNG"
            extension.sqlOutput = findGeneratorProperty(project, "sqlOutput") ?: "database"

            val settings = mutableMapOf<String, String>()
            project.properties.forEach { (t, any) ->
                if (t.startsWith("generator.settings"))
                    settings[t.substringAfter("generator.settings.")] = any.toString()
            }
            extension.settings = settings

            extension.tableNames = (findGeneratorProperty(project, "tableNames")
                ?: "").split(",").asSequence().filter { it.isNotBlank() }.map { it.trim() }
                .distinct()
                .sortedBy { it }.toList()
                .toTypedArray()

            extension.generators = (findGeneratorProperty(project, "generators")
                ?: (if (project.isBoot) "Entity,Service" else "")).split(",").asSequence()
                .filter { it.isNotBlank() }.distinct()
                .map {
                    Class.forName("top.bettercode.summer.gradle.plugin.project.template." + it.trim())
                        .getDeclaredConstructor().newInstance() as Generator
                }.toList().toTypedArray()

            extension.projectIsBoot = project.isBoot
        }

        val extension = project.extensions.getByType(GeneratorExtension::class.java)

        if (project.rootProject.file(extension.pumlSrc).notEmptyDir()) {
            if (!project.rootProject.tasks.names.contains("pumlReformat"))
                configPuml(project.rootProject, extension)
        } else {
            configPuml(project, extension)
        }
    }

    private fun configPuml(project: Project, extension: GeneratorExtension) {
        extension.run { module, tableHolder ->
            val prefix = if (GeneratorExtension.isDefaultModule(module)) "" else "[${
                module.capitalized()
            }]"
            project.tasks.create("printTableNames${prefix}") { task ->
                task.group = PRINT_GROUP
                task.doLast(object : Action<Task> {
                    override fun execute(it: Task) {
                        val tableNames = tableHolder.tableNames()
                        print("${module}数据表:${tableNames.joinToString(",")}")
                    }
                })
            }
        }
        extension.run(if (extension.pdmSources.isNotEmpty()) top.bettercode.summer.tools.generator.DataType.PDM else top.bettercode.summer.tools.generator.DataType.DATABASE) { module, tableHolder ->
            val prefix = if (GeneratorExtension.isDefaultModule(module)) "" else "[${
                module.capitalized()
            }]"
            project.tasks.create("toPuml${prefix}") { task ->
                task.group = PUML_GROUP
                task.doLast(object : Action<Task> {
                    override fun execute(it: Task) {
                        val tables =
                            tableHolder.tables(tableName = extension.tableNames)
                        val plantUML = PlantUML(
                            tables[0].subModuleName,
                            project.file(extension.pumlSrc + "/database/${module}.puml"),
                            null
                        )
                        plantUML.setUp(extension)
                        tables.sortedBy { it.tableName }.forEach { table ->
                            plantUML.run(table)
                        }
                        plantUML.tearDown()
                    }
                })
            }
        }

        project.tasks.create("pumlReformat") { task ->
            task.group = PUML_GROUP
            task.doLast(object : Action<Task> {
                override fun execute(it: Task) {
                    PumlConverter.reformat(extension)
                    PumlConverter.cleanCache()
                }
            })
        }

        project.tasks.create("pumlToDatabase") { task ->
            task.group = PUML_GROUP
            task.doLast(object : Action<Task> {
                override fun execute(it: Task) {
                    PumlConverter.toDatabase(extension)
                    PumlConverter.cleanCache()
                }
            })
        }

        project.tasks.create("pumlToDiagram") { task ->
            task.group = PUML_GROUP
            task.inputs.files(
                File(project.gradle.gradleUserHomeDir, "gradle.properties"),
                project.rootProject.file("gradle.properties")
            )
            val src = extension.file(extension.pumlSrc)
            if (src.exists())
                task.inputs.dir(src)
            val out =
                File(project.rootProject.layout.buildDirectory.get().asFile, extension.pumlSrc)
            if (out.exists())
                task.outputs.dir(out)
            task.doLast(object : Action<Task> {
                override fun execute(it: Task) {
                    extension.pumlSources.forEach { (module, files) ->
                        files.forEach {
                            val sourceFileReader: ISourceFileReader = SourceFileReader(
                                it,
                                File(
                                    out,
                                    module + "/" + extension.pumlDiagramFormat.lowercase(Locale.getDefault())
                                ),
                                "UTF-8"
                            )
                            sourceFileReader.setFileFormatOption(
                                FileFormatOption(
                                    FileFormat.valueOf(
                                        extension.pumlDiagramFormat
                                    )
                                )
                            )
                            try {
                                sourceFileReader.generatedImages
                            } catch (e: Exception) {
                                project.logger.error("${e.javaClass.name}:${e.message}")
                            }
                        }
                    }
                }
            })
        }
        if (extension.hasPuml) {
            val databaseModules = extension.databases.keys
            extension.run { module, tableHolder ->
                if (databaseModules.contains(module)) {
                    val defaultModule = GeneratorExtension.isDefaultModule(module)
                    val suffix = if (defaultModule) "" else "[${
                        module.capitalized()
                    }]"
                    project.tasks.create("toDDL${suffix}") { task ->
                        task.group = PUML_GROUP
                        task.inputs.files(
                            File(project.gradle.gradleUserHomeDir, "gradle.properties"),
                            project.rootProject.file("gradle.properties")
                        )
                        val src = extension.file(extension.pumlSrc)
                        val pdm = extension.file(extension.pdmSrc)
                        if (src.exists())
                            task.inputs.dir(src)
                        if (pdm.exists())
                            task.inputs.file(pdm)
                        task.doLast(object : Action<Task> {
                            override fun execute(it: Task) {
                                MysqlToDDL.useQuote = extension.sqlQuote
                                OracleToDDL.useQuote = extension.sqlQuote
                                MysqlToDDL.useForeignKey = extension.useForeignKey
                                OracleToDDL.useForeignKey = extension.useForeignKey
                                val sqlName = if (defaultModule) "schema" else module
                                val output =
                                    FileUnit("${extension.sqlOutput}/ddl/$sqlName${if (project != project.rootProject) "/${project.name}" else ""}.sql")
                                val tables = tableHolder.tables(tableName = extension.tableNames)
                                val datasource = extension.database(module)
                                when (datasource.driver) {
                                    DatabaseDriver.MYSQL -> MysqlToDDL.toDDL(
                                        tables,
                                        output,
                                        datasource
                                    )

                                    DatabaseDriver.ORACLE -> OracleToDDL.toDDL(
                                        tables,
                                        output,
                                        datasource
                                    )

                                    DatabaseDriver.SQLITE -> SqlLiteToDDL.toDDL(
                                        tables,
                                        output,
                                        datasource
                                    )

                                    else -> {
                                        throw IllegalArgumentException("不支持的数据库")
                                    }
                                }
                                output.writeTo(
                                    if (project.file(extension.sqlOutput)
                                            .exists()
                                    ) project.projectDir else project.rootDir
                                )
                                PumlConverter.cleanCache()
                            }
                        })
                    }
                    project.tasks.create("toDDLUpdate${suffix}") { task ->
                        task.group = PUML_GROUP
                        task.doLast(object : Action<Task> {
                            override fun execute(it: Task) {
                                val database = extension.database(module)
                                database.sshProxy {
                                    MysqlToDDL.useQuote = extension.sqlQuote
                                    OracleToDDL.useQuote = extension.sqlQuote
                                    MysqlToDDL.useForeignKey = extension.useForeignKey
                                    OracleToDDL.useForeignKey = extension.useForeignKey
                                    val deleteTablesWhenUpdate = database.dropTablesWhenUpdate

                                    val databasePumlDir =
                                        extension.file(extension.pumlSrc + "/database")
                                    val unit = FileUnit(
                                        "${extension.sqlOutput}/update/v${project.version}${
                                            if (defaultModule) "" else "-${module}"
                                        }${if (project != project.rootProject) "/${project.name}" else ""}.sql"
                                    )
                                    val allTables = mutableListOf<Table>()
                                    unit.use { pw ->
                                        val tables =
                                            tableHolder.tables(tableName = extension.tableNames)
                                        allTables.addAll(tables)
                                        val tableNames = tables.map { it.tableName }

                                        val currentPumlFile = File(
                                            databasePumlDir,
                                            "current/$module-${project.profilesActive}.puml"
                                        )

                                        val oldTables =
                                            if (database.offline && currentPumlFile.exists()) {
                                                PumlConverter.toTables(database, currentPumlFile)
                                            } else {
                                                if (!database.offline && (tableNames.isNotEmpty() || deleteTablesWhenUpdate)) {
                                                    database.tables(
                                                        tableName = (if (deleteTablesWhenUpdate) emptyList() else tableNames).toTypedArray()
                                                    )
                                                } else emptyList()
                                            }
                                        when (database.driver) {
                                            DatabaseDriver.MYSQL -> MysqlToDDL.toDDLUpdate(
                                                oldTables,
                                                tables,
                                                pw,
                                                database
                                            )

                                            DatabaseDriver.ORACLE -> OracleToDDL.toDDLUpdate(
                                                oldTables,
                                                tables,
                                                pw,
                                                database
                                            )

                                            DatabaseDriver.SQLITE -> SqlLiteToDDL.toDDLUpdate(
                                                oldTables,
                                                tables,
                                                pw,
                                                database
                                            )

                                            else -> {
                                                throw IllegalArgumentException("不支持的数据库")
                                            }
                                        }
                                        if (database.offline) {
                                            val mutableOldTables = oldTables.toMutableList()
                                            for (table in tables) {
                                                mutableOldTables.removeIf { it.tableName == table.tableName }
                                                mutableOldTables.add(table)
                                            }
                                            mutableOldTables.sortBy { it.tableName }
                                            PumlConverter.compile(
                                                database,
                                                mutableOldTables,
                                                currentPumlFile
                                            )
                                        }
                                    }
                                    unit.writeTo(
                                        if (project.file(extension.sqlOutput)
                                                .exists()
                                        ) project.projectDir else project.rootDir
                                    )
                                }
                                PumlConverter.cleanCache()
                            }
                        })
                    }
                    project.tasks.create("toPumlUpdate${suffix}") { task ->
                        task.group = PUML_GROUP
                        task.doLast(object : Action<Task> {
                            override fun execute(it: Task) {
                                if (tableHolder is PumlTableHolder) {
                                    val database = extension.database(module)
                                    database.sshProxy {
                                        MysqlToDDL.useQuote = extension.sqlQuote
                                        OracleToDDL.useQuote = extension.sqlQuote
                                        MysqlToDDL.useForeignKey = extension.useForeignKey
                                        OracleToDDL.useForeignKey = extension.useForeignKey

                                        val databasePumlDir =
                                            extension.file(extension.pumlSrc + "/database")

                                        val currentPumlFile = File(
                                            databasePumlDir,
                                            "current/$module-${project.profilesActive}.puml"
                                        )

                                        val oldTables =
                                            if (database.offline && currentPumlFile.exists()) {
                                                PumlConverter.toTables(database, currentPumlFile)
                                            } else {
                                                database.tables(tableName = emptyArray())
                                            }

                                        tableHolder.files.forEach { file ->
                                            val tables = tableHolder.getTables(file)
                                                .filter { table ->
                                                    val oldTable =
                                                        oldTables.find { it.tableName == table.tableName }
                                                    if (oldTable != null) {
                                                        table.primaryKeyNames =
                                                            oldTable.primaryKeyNames
                                                        table.indexes = oldTable.indexes
                                                        val pumlColumns =
                                                            table.pumlColumns.filterIsInstance<Column>()
                                                        table.pumlColumns = oldTable.pumlColumns
                                                        table.pumlColumns.filterIsInstance<Column>()
                                                            .forEach { col ->
                                                                val newCol =
                                                                    pumlColumns.find { it.columnName == col.columnName }
                                                                if (newCol != null) {
                                                                    col.remarks = newCol.remarks
                                                                    col.idgenerator =
                                                                        newCol.idgenerator
                                                                    col.idgeneratorParam =
                                                                        newCol.idgeneratorParam
                                                                    col.sequence = newCol.sequence
                                                                    col.sequenceStartWith =
                                                                        newCol.sequenceStartWith
                                                                    col.version = newCol.version
                                                                    col.logicalDelete =
                                                                        newCol.logicalDelete
                                                                    col.createdDate =
                                                                        newCol.createdDate
                                                                    col.createdBy = newCol.createdBy
                                                                    col.lastModifiedDate =
                                                                        newCol.lastModifiedDate
                                                                    col.lastModifiedBy =
                                                                        newCol.lastModifiedBy
                                                                    col.asBoolean = newCol.asBoolean
                                                                }
                                                            }
                                                        true
                                                    } else {
                                                        false
                                                    }
                                                }
                                            PumlConverter.compile(database, tables, file)
                                        }
                                    }
                                } else {
                                    project.logger.lifecycle("不支持puml更新")
                                }
                                PumlConverter.cleanCache()
                            }
                        })
                    }
                    project.tasks.create("pumlBuild${suffix}") {
                        it.group = PUML_GROUP
                        it.dependsOn("toDDLUpdate${suffix}", "toDDL${suffix}")
                    }
                }
            }
        }
    }

    private fun findGeneratorProperty(project: Project, key: String) =
        (project.findProperty("generator.${project.name}.$key") as String?
            ?: project.findProperty("generator.$key") as String?)

}