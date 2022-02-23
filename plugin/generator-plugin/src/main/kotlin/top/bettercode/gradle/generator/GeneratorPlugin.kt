package top.bettercode.gradle.generator

import net.sourceforge.plantuml.FileFormat
import net.sourceforge.plantuml.FileFormatOption
import net.sourceforge.plantuml.SourceFileReader
import org.gradle.api.Plugin
import org.gradle.api.Project
import top.bettercode.generator.*
import top.bettercode.generator.database.entity.Table
import top.bettercode.generator.ddl.MysqlToDDL
import top.bettercode.generator.ddl.OracleToDDL
import top.bettercode.generator.ddl.SqlLiteToDDL
import top.bettercode.generator.dsl.Generator
import top.bettercode.generator.dsl.Generators
import top.bettercode.generator.dsl.def.PlantUML
import top.bettercode.generator.powerdesigner.PdmReader
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
                    configuration.catalog = properties["catalog"] as? String ?: ""
                    configuration.schema = properties["schema"] as? String ?: ""
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
                }

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
            extension.basePath = project.projectDir
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
            extension.updateFromType = DataType.valueOf(
                (findProperty(project, "updateFromType")
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
                ?: "").split(",").asSequence().filter { it.isNotBlank() }.map { it.trim() }.distinct()
                .sortedBy { it }.toList()
                .toTypedArray()
            extension.jsonViewIgnoredFieldNames =
                (findProperty(project, "jsonViewIgnoredFieldNames")
                    ?: "deleted,lastModifiedDate").split(",").asSequence()
                    .filter { it.isNotBlank() }.map { it.trim() }.distinct().sortedBy { it }.toList()
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
            if (!project.rootProject.tasks.names.contains("printTableNames"))
                configPuml(project.rootProject, extension)
        } else {
            configPuml(project, extension)
        }
    }

    private fun configPuml(project: Project, extension: GeneratorExtension) {
        project.tasks.create("printTableNames") { task ->
            task.group = taskGroup
            task.doLast {
                val tableNames = Generators.tableNames(extension)
                print(tableNames.joinToString(","))
            }
        }

        project.tasks.create("toPuml") { task ->
            task.group = taskGroup
            task.doLast { _ ->
                val all = extension.tableNames.isEmpty()
                val tableNames = extension.tableNames.toMutableList()
                val toTables = { toTables: () -> List<Table> ->
                    if (all) {
                        toTables()
                    } else {
                        if (tableNames.isNotEmpty()) {
                            val tables = toTables()
                            tableNames.removeAll(tables.map { it.tableName })
                            tables
                        } else
                            emptyList()
                    }
                }
                if (extension.pdmSources.isNotEmpty()) {
                    extension.pdmSources.map { (module, files) ->
                        files.associateBy({ it.nameWithoutExtension }, { file ->
                            toTables {
                                PdmReader.read(
                                    file,
                                    module
                                ).filter { tableNames.contains(it.tableName) }
                            }
                        }).entries
                    }.flatten().associateBy({ it.key }, { it.value })
                } else {
                    extension.datasources.mapValues { (_, jdbc) ->
                        toTables {
                            if (all) {
                                jdbc.tables(jdbc.tableNames())
                            } else {
                                jdbc.tables(tableNames)
                            }
                        }
                    }
                }.filter { it.value.isNotEmpty() }.forEach { (module, tables) ->
                    val plantUML = PlantUML(
                        tables[0].subModule,
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
        project.tasks.create("toDDL") { task ->
            task.group = taskGroup
            task.inputs.files(
                File(project.gradle.gradleUserHomeDir, "gradle.properties"),
                project.rootProject.file("gradle.properties")
            )
            val src = extension.file(extension.pumlSrc)
            val pdm = extension.file(extension.pdmSrc)
            val out = extension.file(extension.sqlOutput)
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
                val toDDl = { m: String, file: File, toTables: (file: File) -> List<Table> ->
                    val outputFile = File(
                        out,
                        "${if (extension.isDefaultModule(m)) "ddl" else "ddl-${m}"}/${file.name}"
                    )
                    val jdbc = extension.datasources[m]
                        ?: throw IllegalStateException("未配置${m}模块数据库信息")
                    when (jdbc.databaseDriver) {
                        DatabaseDriver.MYSQL -> MysqlToDDL.toDDL(
                            toTables(file),
                            outputFile
                        )
                        DatabaseDriver.ORACLE -> OracleToDDL.toDDL(
                            toTables(file),
                            outputFile
                        )
                        DatabaseDriver.SQLITE -> SqlLiteToDDL.toDDL(
                            toTables(file),
                            outputFile
                        )
                        else -> {
                            throw IllegalArgumentException("不支持的数据库")
                        }
                    }
                }
                when (extension.dataType) {
                    DataType.PUML -> {
                        extension.pumlSources.forEach { (module, files) ->
                            files.forEach { file ->
                                toDDl(module, file) {
                                    PumlConverter.toTables(
                                        it,
                                        module
                                    )
                                }
                            }
                        }
                    }
                    DataType.PDM -> {
                        extension.pdmSources.forEach { (module, files) ->
                            files.forEach { file ->
                                toDDl(module, file) {
                                    PdmReader.read(it, module)
                                }
                            }
                        }
                    }
                    else -> {
                        throw IllegalArgumentException("不支持数据结构源")
                    }
                }
            }
        }
        project.tasks.create("toDDLUpdate") { task ->
            task.group = taskGroup
            task.doLast {
                MysqlToDDL.useQuote = extension.sqlQuote
                OracleToDDL.useQuote = extension.sqlQuote
                MysqlToDDL.useForeignKey = extension.useForeignKey
                OracleToDDL.useForeignKey = extension.useForeignKey
                val deleteTablesWhenUpdate = extension.dropTablesWhenUpdate

                val databasePumlDir = extension.file(extension.pumlSrc + "/database")
                val out = extension.file(extension.sqlOutput)

                val toDDLUpdate =
                    { module: String, files: List<File>, toTables: (file: File) -> List<Table> ->
                        val databaseFile = File(databasePumlDir, "${module}.puml")
                        val updateFile = File(
                            out,
                            "${if (extension.isDefaultModule(module)) "update" else "update-${module}"}/v${project.version}.sql"
                        )
                        val allTables = mutableListOf<Table>()
                        updateFile.printWriter().use { pw ->
                            val tables = files.map { file ->
                                toTables(file)
                            }.flatMap { it.asIterable() }

                            allTables.addAll(tables)
                            val jdbc = extension.datasources[module]
                                ?: throw IllegalStateException("未配置${module}模块数据库信息")
                            val tableNames = tables.map { it.tableName }
                            val oldTables =
                                if (deleteTablesWhenUpdate) {
                                    if (DataType.PUML == extension.updateFromType && databaseFile.exists()) {
                                        PumlConverter.toTables(databaseFile, module)
                                    } else {
                                        jdbc.tables(jdbc.tableNames())
                                    }
                                } else {
                                    if (DataType.PUML == extension.updateFromType && databaseFile.exists()) {
                                        PumlConverter.toTables(databaseFile, module)
                                    } else {
                                        jdbc.tables(tableNames)
                                    }
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
                        if (DataType.PUML == extension.updateFromType)
                            PumlConverter.compile(extension, allTables, databasePumlDir)
                    }
                when (extension.dataType) {
                    DataType.PUML -> {
                        extension.pumlSources.forEach { (module, files) ->
                            toDDLUpdate(module, files) {
                                PumlConverter.toTables(it, module)
                            }
                        }
                    }
                    DataType.PDM -> {
                        extension.pdmSources.forEach { (module, files) ->
                            toDDLUpdate(module, files) {
                                PdmReader.read(it, module)
                            }
                        }
                    }
                    else -> {
                        throw IllegalArgumentException("不支持数据结构源")
                    }
                }

            }
        }
        project.tasks.create("pumlBuild") {
            it.group = taskGroup
            it.dependsOn("toDDLUpdate", "toDDL")
        }
    }

    private fun findProperty(project: Project, key: String) =
        (project.findProperty("generator.${project.name}.$key") as? String
            ?: project.findProperty("generator.$key") as? String)

}