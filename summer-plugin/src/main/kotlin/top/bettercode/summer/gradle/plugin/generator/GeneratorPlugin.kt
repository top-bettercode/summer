package top.bettercode.summer.gradle.plugin.generator

import isBoot
import isCore
import net.sourceforge.plantuml.FileFormat
import net.sourceforge.plantuml.FileFormatOption
import net.sourceforge.plantuml.ISourceFileReader
import net.sourceforge.plantuml.SourceFileReader
import org.gradle.api.Action
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.Task
import top.bettercode.summer.tools.generator.DatabaseDriver
import top.bettercode.summer.tools.generator.GeneratorExtension
import top.bettercode.summer.tools.generator.GeneratorExtension.Companion.defaultModuleName
import top.bettercode.summer.tools.generator.JDBCConnectionConfiguration
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
class GeneratorPlugin : Plugin<Project> {

    companion object {
        const val genGroup = "gen"
        const val printGroup = "print"
        const val pumlGroup = "puml"
    }

    override fun apply(project: Project) {
        project.extensions.create("generator", GeneratorExtension::class.java)
        project.extensions.configure(GeneratorExtension::class.java) { extension ->

            val entries = project.properties.filter { it.key.startsWith("datasource.") }.entries
            extension.datasources =
                    ((if (project.properties.containsKey("datasource.url")) mapOf(defaultModuleName to (entries.filter {
                        it.key.split('.').size == 2
                    }.associateBy({ it.key.substringAfter("datasource.") }, { it.value }))
                    ) else emptyMap()) + entries.filter { it.key.split('.').size == 3 }.groupBy {
                        it.key.substringAfter("datasource.").substringBefore(".")
                    }.mapValues { entry ->
                        entry.value.groupBy({ it.key.substringAfter("datasource.${entry.key}.") },
                                { it.value }).mapValues { it.value.last() }
                    }).mapValues { (module, properties) ->
                        val configuration = JDBCConnectionConfiguration()
                        configuration.module = module
                        configuration.url = properties["url"] as? String ?: ""
                        configuration.databaseDriver =
                                DatabaseDriver.fromProductName(properties["productName"] as? String ?: "")
                        configuration.catalog = properties["catalog"] as? String?
                        configuration.schema = properties["schema"] as? String?
                        configuration.username = properties["username"] as? String ?: "root"
                        configuration.password = properties["password"] as? String ?: "root"
                        configuration.driverClass = properties["driverClass"] as? String ?: ""
                        configuration.debug = (properties["debug"] as? String ?: "false").toBoolean()
                        configuration.queryIndex =
                                (properties["queryIndex"] as? String ?: "true").toBoolean()

                        configuration.tinyInt1isBit =
                                (properties["tinyInt1isBit"] as? String ?: "false").toBoolean()
                        if (configuration.isOracle) {
                            configuration.properties["oracle.net.CONNECT_TIMEOUT"] = "10000"
                        }

                        configuration.entityPrefix =
                                properties["entityPrefix"] as? String ?: findGeneratorProperty(
                                        project,
                                        "entityPrefix"
                                ) ?: ""

                        configuration.tablePrefixes =
                                (properties["tablePrefix"] as? String ?: findGeneratorProperty(
                                        project,
                                        "tablePrefix"
                                ) ?: "").split(",")
                                        .filter { it.isNotBlank() }.toTypedArray()

                        configuration.tableSuffixes =
                                (properties["tableSuffix"] as? String ?: findGeneratorProperty(
                                        project,
                                        "tableSuffix"
                                ) ?: "").split(",")
                                        .filter { it.isNotBlank() }.toTypedArray()

                        configuration
                    }.toSortedMap(kotlin.Comparator { o1, o2 -> o1.compareTo(o2) })

            extension.delete = (findGeneratorProperty(project, "delete"))?.toBoolean() ?: false
            extension.projectPackage =
                    (findGeneratorProperty(project, "project-package"))?.toBoolean()
                            ?: false
            extension.dropTablesWhenUpdate =
                    (findGeneratorProperty(project, "dropTablesWhenUpdate"))?.toBoolean()
                            ?: false
            extension.dropColumnsWhenUpdate =
                    (findGeneratorProperty(project, "dropColumnsWhenUpdate"))?.toBoolean()
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
                            ?: project.findProperty("app.packageName") as? String
                            ?: "")
            extension.userModule =
                    (findGeneratorProperty(project, "userModule"))?.toBoolean() ?: true
            extension.applicationName = project.findProperty("application.name") as? String
                    ?: project.rootProject.name
            extension.projectName =
                    (findGeneratorProperty(project, "projectName") ?: project.name)
            extension.isCore = project.isCore

            extension.primaryKeyName = findGeneratorProperty(project, "primaryKeyName") ?: "id"
            extension.remarks = findGeneratorProperty(project, "remarks") ?: ""
            extension.softDeleteColumnName = findGeneratorProperty(project, "softDeleteColumnName")
                    ?: "deleted"
            extension.commonCodeTypes = (findGeneratorProperty(project, "commonCodeTypes")
                    ?: extension.softDeleteColumnName).split(",").asSequence()
                    .filter { it.isNotBlank() }.map { it.trim() }.toList()
                    .toTypedArray()
            extension.softDeleteAsBoolean =
                    (findGeneratorProperty(project, "softDeleteAsBoolean"))?.toBoolean()
                            ?: true
            extension.dataType = top.bettercode.summer.tools.generator.DataType.valueOf(
                    (findGeneratorProperty(project, "dataType")
                            ?: top.bettercode.summer.tools.generator.DataType.DATABASE.name).toUpperCase(
                            Locale.getDefault()
                    )
            )
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
            extension.excludeTableNames = (findGeneratorProperty(project, "excludeTableNames")
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

        if (project.rootProject.file(extension.pumlSrc).exists()) {
            if (!project.rootProject.tasks.names.contains("pumlReformat"))
                configPuml(project.rootProject, extension)
        } else {
            configPuml(project, extension)
        }
    }

    private fun configPuml(project: Project, extension: GeneratorExtension) {
        extension.run { module, tableHolder ->
            val prefix = if (extension.isDefaultModule(module)) "" else "[${
                module.capitalized()
            }]"
            project.tasks.create("printTableNames${prefix}") { task ->
                task.group = printGroup
                task.doLast(object : Action<Task> {
                    override fun execute(it: Task) {
                        val tableNames = tableHolder.tableNames()
                        print("${module}数据表:${tableNames.joinToString(",")}")
                    }
                })
            }
        }
        extension.run(if (extension.pdmSources.isNotEmpty()) top.bettercode.summer.tools.generator.DataType.PDM else top.bettercode.summer.tools.generator.DataType.DATABASE) { module, tableHolder ->
            val prefix = if (extension.isDefaultModule(module)) "" else "[${
                module.capitalized()
            }]"
            project.tasks.create("toPuml${prefix}") { task ->
                task.group = pumlGroup
                task.doLast(object : Action<Task> {
                    override fun execute(it: Task) {
                        val tables =
                                tableHolder.tables(tableName = *extension.tableNames)
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
            task.group = pumlGroup
            task.doLast(object : Action<Task> {
                override fun execute(it: Task) {
                    PumlConverter.reformat(extension)
                }
            })
        }

        project.tasks.create("pumlToDatabase") { task ->
            task.group = pumlGroup
            task.doLast(object : Action<Task> {
                override fun execute(it: Task) {
                    PumlConverter.toDatabase(extension)
                }
            })
        }

        project.tasks.create("pumlToDiagram") { task ->
            task.group = pumlGroup
            task.inputs.files(
                    File(project.gradle.gradleUserHomeDir, "gradle.properties"),
                    project.rootProject.file("gradle.properties")
            )
            val src = extension.file(extension.pumlSrc)
            if (src.exists())
                task.inputs.dir(src)
            val out = File(project.rootProject.buildDir, extension.pumlSrc)
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
                                            module + "/" + extension.pumlDiagramFormat.toLowerCase(Locale.getDefault())
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
                                println("${e.javaClass.name}:${e.message}")
                            }
                        }
                    }
                }
            })
        }
        if (extension.dataType != top.bettercode.summer.tools.generator.DataType.DATABASE) {
            val datasourceModules = extension.datasources.keys
            extension.run { module, tableHolder ->
                if (datasourceModules.contains(module)) {
                    val defaultModule = extension.isDefaultModule(module)
                    val suffix = if (defaultModule) "" else "[${
                        module.capitalized()
                    }]"
                    project.tasks.create("toDDL${suffix}") { task ->
                        task.group = pumlGroup
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
                                val output = FileUnit("${extension.sqlOutput}/ddl/$sqlName.sql")
                                val jdbc = extension.datasources[module]
                                        ?: throw IllegalStateException("未配置${module}模块数据库信息")
                                val tables = tableHolder.tables(tableName = *extension.tableNames)
                                when (jdbc.databaseDriver) {
                                    DatabaseDriver.MYSQL -> MysqlToDDL.toDDL(
                                            tables,
                                            output
                                    )

                                    DatabaseDriver.ORACLE -> OracleToDDL.toDDL(
                                            tables,
                                            output
                                    )

                                    DatabaseDriver.SQLITE -> SqlLiteToDDL.toDDL(
                                            tables,
                                            output
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
                            }
                        })
                    }
                    project.tasks.create("toDDLUpdate${suffix}") { task ->
                        task.group = pumlGroup
                        task.doLast(object : Action<Task> {
                            override fun execute(it: Task) {
                                extension.dropTablesWhenUpdate = (findGeneratorProperty(
                                        project,
                                        "dropTablesWhenUpdate"
                                ))?.toBoolean()
                                        ?: false
                                MysqlToDDL.useQuote = extension.sqlQuote
                                OracleToDDL.useQuote = extension.sqlQuote
                                MysqlToDDL.useForeignKey = extension.useForeignKey
                                OracleToDDL.useForeignKey = extension.useForeignKey
                                val deleteTablesWhenUpdate = extension.dropTablesWhenUpdate

                                val databasePumlDir =
                                        extension.file(extension.pumlSrc + "/database")
                                val databaseFile = File(databasePumlDir, "${module}.puml")
                                val unit =
                                        FileUnit(
                                                "${extension.sqlOutput}/update/v${project.version}${
                                                    if (defaultModule) "" else "-${module}"
                                                }.sql"
                                        )
                                val allTables = mutableListOf<Table>()
                                unit.use { pw ->
                                    val jdbc = extension.datasources[module]
                                            ?: throw IllegalStateException("未配置${module}模块数据库信息")
                                    val tables =
                                            tableHolder.tables(tableName = *extension.tableNames)
                                    allTables.addAll(tables)
                                    val tableNames = tables.map { it.tableName }
                                    val oldTables = if (databaseFile.exists()) {
                                        PumlConverter.toTables(databaseFile) {
                                            it.ext = extension
                                            it.module = module
                                        }
                                    } else {
                                        jdbc.tables(
                                                tableName =
                                                *(if (deleteTablesWhenUpdate) jdbc.tableNames()
                                                else tableNames).toTypedArray()
                                        )
                                    }
                                    when (jdbc.databaseDriver) {
                                        DatabaseDriver.MYSQL -> MysqlToDDL.toDDLUpdate(
                                                module, oldTables, tables, pw, extension
                                        )

                                        DatabaseDriver.ORACLE -> OracleToDDL.toDDLUpdate(
                                                module, oldTables, tables, pw, extension
                                        )

                                        DatabaseDriver.SQLITE -> SqlLiteToDDL.toDDLUpdate(
                                                module, oldTables, tables, pw, extension
                                        )

                                        else -> {
                                            throw IllegalArgumentException("不支持的数据库")
                                        }
                                    }
                                }
                                unit.writeTo(
                                        if (project.file(extension.sqlOutput)
                                                        .exists()
                                        ) project.projectDir else project.rootDir
                                )
                            }
                        })
                    }
                    project.tasks.create("pumlBuild${suffix}") {
                        it.group = pumlGroup
                        it.dependsOn("toDDLUpdate${suffix}", "toDDL${suffix}")
                    }
                }
            }
        }
    }

    private fun findGeneratorProperty(project: Project, key: String) =
            (project.findProperty("generator.${project.name}.$key") as? String
                    ?: project.findProperty("generator.$key") as? String)

}