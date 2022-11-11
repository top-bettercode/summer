package top.bettercode.gradle.generator

import net.sourceforge.plantuml.FileFormat
import net.sourceforge.plantuml.FileFormatOption
import net.sourceforge.plantuml.ISourceFileReader
import net.sourceforge.plantuml.SourceFileReader
import org.gradle.api.Action
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.Task
import top.bettercode.generator.DataType
import top.bettercode.generator.DatabaseDriver
import top.bettercode.generator.GeneratorExtension
import top.bettercode.generator.GeneratorExtension.Companion.defaultModuleName
import top.bettercode.generator.JDBCConnectionConfiguration
import top.bettercode.generator.database.entity.Table
import top.bettercode.generator.ddl.MysqlToDDL
import top.bettercode.generator.ddl.OracleToDDL
import top.bettercode.generator.ddl.SqlLiteToDDL
import top.bettercode.generator.dom.unit.FileUnit
import top.bettercode.generator.dsl.Generator
import top.bettercode.generator.dsl.Generators
import top.bettercode.generator.dsl.def.PlantUML
import top.bettercode.generator.puml.PumlConverter
import top.bettercode.gradle.generator.ProjectUtil.isBoot
import top.bettercode.gradle.generator.ProjectUtil.isCore
import top.bettercode.lang.capitalized
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
                (mapOf(defaultModuleName to (entries.filter { it.key.split('.').size == 2 }
                    .associateBy({ it.key.substringAfter("datasource.") }, { it.value })
                        )
                ) + entries.filter { it.key.split('.').size == 3 }.groupBy {
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
                }.toSortedMap { o1, o2 -> o1.compareTo(o2) }

            extension.unitedDatasource =
                (findGeneratorProperty(project, "singleDatasource"))?.toBoolean()
                    ?: true
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
            extension.packageName = findGeneratorProperty(project, "packageName")
                ?: project.findProperty("app.packageName") as? String ?: ""
            extension.userModule =
                (findGeneratorProperty(project, "userModule"))?.toBoolean() ?: true
            extension.applicationName = project.findProperty("application.name") as? String
                ?: project.rootProject.name
            extension.projectName =
                (findGeneratorProperty(project, "projectName") ?: project.name).replace(
                    "-",
                    ""
                )
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
            extension.dataType = DataType.valueOf(
                (findGeneratorProperty(project, "dataType")
                    ?: DataType.DATABASE.name).uppercase(Locale.getDefault())
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

            extension.generators = (findGeneratorProperty(project, "generators")
                ?: "").split(",").asSequence().filter { it.isNotBlank() }.distinct()
                .map {
                    Class.forName(it.trim()).getDeclaredConstructor().newInstance() as Generator
                }.toList().toTypedArray()

            extension.projectIsBoot = project.isBoot
        }

        val extension = project.extensions.getByType(GeneratorExtension::class.java)

        if (extension.moduleSize > 1)
            project.tasks.create("gen[[[All]") { task ->
                task.group = genGroup
                task.doLast(object : Action<Task> {
                    override fun execute(it: Task) {
                        extension.run { module, tableHolder ->
                            println("生成$module")
                            Generators.call(
                                project.extensions.getByType(GeneratorExtension::class.java),
                                tableHolder
                            )
                        }
                    }
                })
            }

        extension.run { module, tableHolder ->
            val prefix = if (defaultModuleName == module) "" else "[[${
                module.capitalized()
            }]"
            project.tasks.create("gen${prefix}") { task ->
                task.group = genGroup
                task.doLast(object : Action<Task> {
                    override fun execute(it: Task) {
                        Generators.call(
                            project.extensions.getByType(GeneratorExtension::class.java),
                            tableHolder
                        )
                    }
                })
            }
        }

        if (extension.unitedDatasource) {
            if (!project.rootProject.tasks.names.contains("print[TableNames]"))
                configPuml(project.rootProject, extension)
        } else {
            configPuml(project, extension)
        }
    }

    private fun configPuml(project: Project, extension: GeneratorExtension) {

        extension.run { module, tableHolder ->
            val prefix = if (defaultModuleName == module) "" else "[${
                module.capitalized()
            }]"
            project.tasks.create("print[TableNames]${prefix}") { task ->
                task.group = printGroup
                task.doLast(object : Action<Task> {
                    override fun execute(it: Task) {
                        val tableNames = tableHolder.tableNames()
                        print("${module}数据表:${tableNames.joinToString(",")}")
                    }
                })
            }
        }
        extension.run(if (extension.pdmSources.isNotEmpty()) DataType.PDM else DataType.DATABASE) { module, tableHolder ->
            val prefix = if (defaultModuleName == module) "" else "[${
                module.capitalized()
            }]"
            project.tasks.create("toPuml${prefix}") { task ->
                task.group = pumlGroup
                task.doLast(object : Action<Task> {
                    override fun execute(it: Task) {
                        val tables =
                            tableHolder.tables(tableName = extension.tableNames)
                        val plantUML = PlantUML(
                            tables[0].subModuleName,
                            File(
                                extension.file(extension.pumlSrc),
                                "database/${module}.puml"
                            ),
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
                                println("${e.javaClass.name}:${e.message}")
                            }
                        }
                    }
                }
            })
        }
        if (extension.dataType != DataType.DATABASE) {
            val datasourceModules = extension.datasources.keys
            extension.run { module, tableHolder ->
                if (datasourceModules.contains(module)) {
                    val prefix = if (defaultModuleName == module) "" else "[${
                        module.capitalized()
                    }]"
                    project.tasks.create("toDDL${prefix}") { task ->
                        task.group = pumlGroup
                        task.inputs.files(
                            File(project.gradle.gradleUserHomeDir, "gradle.properties"),
                            project.rootProject.file("gradle.properties")
                        )
                        val src = extension.file(extension.pumlSrc)
                        val pdm = extension.file(extension.pdmSrc)
                        val out = project.rootProject.file("${extension.sqlOutput}/ddl")
                        if (src.exists())
                            task.inputs.dir(src)
                        if (pdm.exists())
                            task.inputs.file(pdm)
                        if (out.exists())
                            task.outputs.dir(out)
                        task.doLast(object : Action<Task> {
                            override fun execute(it: Task) {
                                MysqlToDDL.useQuote = extension.sqlQuote
                                OracleToDDL.useQuote = extension.sqlQuote
                                MysqlToDDL.useForeignKey = extension.useForeignKey
                                OracleToDDL.useForeignKey = extension.useForeignKey
                                val sqlName = if (defaultModuleName == module) "schema" else module
                                val output = FileUnit("${extension.sqlOutput}/ddl/$sqlName.sql")
                                val jdbc = extension.datasources[module]
                                    ?: throw IllegalStateException("未配置${module}模块数据库信息")
                                val tables = tableHolder.tables(tableName = extension.tableNames)
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
                                output.writeTo(project.rootDir)
                            }
                        })
                    }
                    project.tasks.create("toDDLUpdate${prefix}") { task ->
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
                                            if (extension.isDefaultModule(module)) "" else "-${module}"
                                        }.sql"
                                    )
                                val allTables = mutableListOf<Table>()
                                unit.use { pw ->
                                    val tables =
                                        tableHolder.tables(tableName = extension.tableNames)
                                    allTables.addAll(tables)
                                    val jdbc = extension.datasources[module]
                                        ?: throw IllegalStateException("未配置${module}模块数据库信息")
                                    val tableNames = tables.map { it.tableName }
                                    val oldTables = if (databaseFile.exists()) {
                                        PumlConverter.toTables(databaseFile) {
                                            it.ext = extension
                                            it.module = module
                                        }
                                    } else {
                                        jdbc.tables(
                                            tableName =
                                            (if (deleteTablesWhenUpdate) jdbc.tableNames()
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
                                unit.writeTo(project.rootDir)
                            }
                        })
                    }
                    project.tasks.create("pumlBuild${prefix}") {
                        it.group = pumlGroup
                        it.dependsOn("toDDLUpdate${prefix}", "toDDL${prefix}")
                    }
                }
            }
        }
    }

    private fun findGeneratorProperty(project: Project, key: String) =
        (project.findProperty("generator.${project.name}.$key") as? String
            ?: project.findProperty("generator.$key") as? String)

}