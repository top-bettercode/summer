package top.bettercode.gradle.generator

import net.sourceforge.plantuml.FileFormat
import net.sourceforge.plantuml.FileFormatOption
import net.sourceforge.plantuml.SourceFileReader
import org.gradle.api.Plugin
import org.gradle.api.Project
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
import java.io.File


/**
 *
 * 注册task
 *
 * @author Peter Wu
 */
class GeneratorPlugin : Plugin<Project> {

    companion object {
        const val taskGroup = "gen"
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
                    configuration.catalog = properties["catalog"] as? String?
                    configuration.schema = properties["schema"] as? String?
                    configuration.username = properties["username"] as? String ?: "root"
                    configuration.password = properties["password"] as? String ?: "root"
                    configuration.driverClass = properties["driverClass"] as? String ?: ""
                    configuration.debug = (properties["debug"] as? String ?: "false").toBoolean()
                    configuration.queryIndex =
                        (properties["queryIndex"] as? String ?: "true").toBoolean()
                    if (configuration.isOracle) {
                        configuration.properties["oracle.net.CONNECT_TIMEOUT"] = "10000"
                    }
                    configuration
                }.toSortedMap(Comparator { o1, o2 -> o1.compareTo(o2) })

            extension.unitedDatasource = (findProperty(project, "singleDatasource"))?.toBoolean()
                ?: true
            extension.delete = (findProperty(project, "delete"))?.toBoolean() ?: false
            extension.projectPackage = (findProperty(project, "project-package"))?.toBoolean()
                ?: false
            extension.dropTablesWhenUpdate =
                (findProperty(project, "dropTablesWhenUpdate"))?.toBoolean()
                    ?: false
            extension.dropColumnsWhenUpdate =
                (findProperty(project, "dropColumnsWhenUpdate"))?.toBoolean()
                    ?: false
            extension.useJSR310Types =
                (findProperty(project, "useJSR310Types"))?.toBoolean() ?: true
            extension.replaceAll = (findProperty(project, "replaceAll"))?.toBoolean() ?: false
            extension.useForeignKey = (findProperty(project, "useForeignKey"))?.toBoolean() ?: false
            extension.sqlQuote = (findProperty(project, "sqlQuote"))?.toBoolean() ?: true
            extension.rootPath = project.rootDir
            extension.projectDir = project.projectDir
            extension.dir = findProperty(project, "dir") ?: "src/main/java"
            extension.packageName = findProperty(project, "packageName")
                ?: project.findProperty("app.packageName") as? String ?: ""
            extension.userModule = (findProperty(project, "userModule"))?.toBoolean() ?: true
            extension.applicationName = project.findProperty("application.name") as? String
                ?: project.rootProject.name
            extension.projectName = findProperty(project, "projectName") ?: project.name
            extension.primaryKeyName = findProperty(project, "primaryKeyName") ?: "id"
            extension.tablePrefixes =
                (findProperty(project, "tablePrefix") ?: "").split(",").filter { it.isNotBlank() }
                    .toTypedArray()
            extension.remarks = findProperty(project, "remarks") ?: ""
            extension.softDeleteColumnName = findProperty(project, "softDeleteColumnName")
                ?: "deleted"
            extension.commonCodeTypes = (findProperty(project, "commonCodeTypes")
                ?: extension.softDeleteColumnName).split(",").asSequence()
                .filter { it.isNotBlank() }.map { it.trim() }.toList()
                .toTypedArray()
            extension.softDeleteAsBoolean =
                (findProperty(project, "softDeleteAsBoolean"))?.toBoolean()
                    ?: true
            extension.idgenerator =
                findProperty(project, "idgenerator") ?: "uuid2"
            extension.dataType = DataType.valueOf(
                (findProperty(project, "dataType")
                    ?: DataType.DATABASE.name).toUpperCase()
            )
            //puml
            extension.pumlSrc = findProperty(project, "puml.src") ?: "puml"
            extension.pumlDiagramFormat = findProperty(project, "puml.diagramFormat") ?: "PNG"
            extension.sqlOutput = findProperty(project, "sqlOutput") ?: "database"

            val settings = mutableMapOf<String, String>()
            project.properties.forEach { (t, any) ->
                if (t.startsWith("generator.settings"))
                    settings[t.substringAfter("generator.settings.")] = any.toString()
            }
            extension.settings = settings

            extension.tableNames = (findProperty(project, "tableNames")
                ?: "").split(",").asSequence().filter { it.isNotBlank() }.map { it.trim() }
                .distinct()
                .sortedBy { it }.toList()
                .toTypedArray()
            extension.jsonViewIgnoredFieldNames =
                (findProperty(project, "jsonViewIgnoredFieldNames")
                    ?: "deleted,lastModifiedDate").split(",").asSequence()
                    .filter { it.isNotBlank() }.map { it.trim() }.distinct().sortedBy { it }
                    .toList()
                    .toTypedArray()

            extension.generators = (findProperty(project, "generators")
                ?: "").split(",").asSequence().filter { it.isNotBlank() }.distinct()
                .map {
                    Class.forName(it.trim()).getDeclaredConstructor().newInstance() as Generator
                }.toList().toTypedArray()
        }

        project.tasks.create("gen") { task ->
            task.group = taskGroup
            task.doLast {
                Generators.call(project.extensions.getByType(GeneratorExtension::class.java))
            }
        }

        val extension = project.extensions.getByType(GeneratorExtension::class.java)
        if (extension.unitedDatasource) {
            if (!project.rootProject.tasks.names.contains("print[TableNames]"))
                configPuml(project.rootProject, extension)
        } else {
            configPuml(project, extension)
        }
    }

    private fun configPuml(project: Project, extension: GeneratorExtension) {

        extension.run { module, tableHolder ->
            val prefix = if (defaultModuleName == module) "" else "[${module.capitalize()}]"
            project.tasks.create("print[TableNames]${prefix}") { task ->
                task.group = taskGroup
                task.doLast {
                    val tableNames = tableHolder.tableNames()
                    print("${module}数据表:${tableNames}")
                }
            }
        }
        extension.run(if (extension.pdmSources.isNotEmpty()) DataType.PDM else DataType.DATABASE) { module, tableHolder ->
            val prefix = if (defaultModuleName == module) "" else module.capitalize()
            project.tasks.create("toPuml${prefix}") { task ->
                task.group = taskGroup
                task.doLast { _ ->
                    val tables = tableHolder.tables(*extension.tableNames)
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
            }
        }

        project.tasks.create("pumlReformat") { task ->
            task.group = taskGroup
            task.doLast {
                PumlConverter.reformat(extension)
            }
        }

        project.tasks.create("pumlToDiagram") { task ->
            task.group = taskGroup
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
            task.doLast { _ ->
                extension.pumlSources.forEach { (module, files) ->
                    files.forEach {
                        val sourceFileReader = SourceFileReader(
                            it,
                            File(out, module + "/" + extension.pumlDiagramFormat.toLowerCase()),
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
        }
        if (extension.dataType != DataType.DATABASE)
            extension.run { module, tableHolder ->
                val prefix = if (defaultModuleName == module) "" else module.capitalize()
                project.tasks.create("ddl${prefix}") { task ->
                    task.group = taskGroup
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
                    task.doLast {
                        MysqlToDDL.useQuote = extension.sqlQuote
                        OracleToDDL.useQuote = extension.sqlQuote
                        MysqlToDDL.useForeignKey = extension.useForeignKey
                        OracleToDDL.useForeignKey = extension.useForeignKey
                        val output = FileUnit("${extension.sqlOutput}/ddl/$module.sql")
                        val jdbc = extension.datasources[module]
                            ?: throw IllegalStateException("未配置${module}模块数据库信息")
                        val tables = tableHolder.tables(*extension.tableNames)
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
                }
                project.tasks.create("ddlUpdate${prefix}") { task ->
                    task.group = taskGroup
                    task.doLast {
                        MysqlToDDL.useQuote = extension.sqlQuote
                        OracleToDDL.useQuote = extension.sqlQuote
                        MysqlToDDL.useForeignKey = extension.useForeignKey
                        OracleToDDL.useForeignKey = extension.useForeignKey
                        val deleteTablesWhenUpdate = extension.dropTablesWhenUpdate

                        val databasePumlDir = extension.file(extension.pumlSrc + "/database")
                        val databaseFile = File(databasePumlDir, "${module}.puml")
                        val unit =
                            FileUnit(
                                "${extension.sqlOutput}/${
                                    if (extension.isDefaultModule(
                                            module
                                        )
                                    ) "update" else "update-${module}"
                                }/v${project.version}.sql"
                            )
                        val allTables = mutableListOf<Table>()
                        unit.use { pw ->
                            val tables = tableHolder.tables(*extension.tableNames)
                            allTables.addAll(tables)
                            val jdbc = extension.datasources[module]
                                ?: throw IllegalStateException("未配置${module}模块数据库信息")
                            val tableNames = tables.map { it.tableName }
                            val oldTables = if (databaseFile.exists()) {
                                PumlConverter.toTables(databaseFile, module)
                            } else {
                                jdbc.tables(
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
                        unit.writeTo(project.rootDir)
                    }
                }
                project.tasks.create("ddlBuild${prefix}") {
                    it.group = taskGroup
                    it.dependsOn("ddlUpdate${prefix}", "ddl${prefix}")
                }
            }
    }

    private fun findProperty(project: Project, key: String) =
        (project.findProperty("generator.${project.name}.$key") as? String
            ?: project.findProperty("generator.$key") as? String)

}