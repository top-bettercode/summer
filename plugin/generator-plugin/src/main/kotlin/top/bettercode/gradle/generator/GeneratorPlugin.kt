package top.bettercode.gradle.generator

import net.sourceforge.plantuml.FileFormat
import net.sourceforge.plantuml.FileFormatOption
import net.sourceforge.plantuml.SourceFileReader
import org.gradle.api.Action
import org.gradle.api.Plugin
import org.gradle.api.Project
import top.bettercode.generator.DataType
import top.bettercode.generator.GeneratorExtension
import top.bettercode.generator.JDBCConnectionConfiguration
import top.bettercode.generator.database.entity.Table
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

            extension.datasource(Action {
                it.url = findDatasourceProperty(project, "url") ?: ""
                it.catalog = findDatasourceProperty(project, "catalog")
                it.schema = findDatasourceProperty(project, "schema")
                it.username = findDatasourceProperty(project, "username") ?: "root"
                it.password = findDatasourceProperty(project, "password") ?: "root"
                it.driverClass = findDatasourceProperty(project, "driverClass") ?: ""
                if (it.isOracle) {
                    it.properties["oracle.net.CONNECT_TIMEOUT"] = "10000"
                }
            })
            extension.singleDatasource = (findProperty(project, "singleDatasource"))?.toBoolean()
                ?: true
            extension.delete = (findProperty(project, "delete"))?.toBoolean() ?: false
            extension.debug = (findProperty(project, "debug"))?.toBoolean() ?: false
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
            extension.module = findProperty(project, "module") ?: "modules"
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
            extension.queryIndex = (findProperty(project, "queryIndex"))?.toBoolean() ?: true
            extension.dataType = DataType.valueOf(
                (findProperty(project, "dataType")
                    ?: DataType.DATABASE.name).toUpperCase()
            )
            extension.updateFromType = DataType.valueOf(
                (findProperty(project, "updateFromType")
                    ?: DataType.DATABASE.name).toUpperCase()
            )
            //puml
            extension.pumlSrc = findProperty(project, "puml.src") ?: "puml/src"
            val pumlDatabaseDriver = findProperty(project, "puml.databaseDriver")
                ?: extension.datasource.databaseDriver.id
            extension.pumlDatabaseDriver =
                top.bettercode.generator.DatabaseDriver.fromProductName(pumlDatabaseDriver)
            extension.pumlDatabase = findProperty(project, "puml.database") ?: "puml/database"
            extension.pumlDiagramFormat = findProperty(project, "puml.diagramFormat") ?: "PNG"
            extension.sqlOutput = findProperty(project, "sqlOutput") ?: "database"
            extension.updateSqlOutput =
                findProperty(project, "updateSqlOutput") ?: "v${project.version}.sql"

            val settings = mutableMapOf<String, String>()
            project.properties.forEach { (t, any) ->
                if (t.startsWith("generator.settings"))
                    settings[t.substringAfter("generator.settings.")] = any.toString()
            }
            extension.settings = settings

            extension.tableNames = (findProperty(project, "tableNames")
                ?: "").split(",").asSequence().filter { it.isNotBlank() }.map { it.trim() }
                .sortedBy { it }.toList()
                .toTypedArray()
            extension.jsonViewIgnoredFieldNames =
                (findProperty(project, "jsonViewIgnoredFieldNames")
                    ?: "deleted,lastModifiedDate").split(",").asSequence()
                    .filter { it.isNotBlank() }.map { it.trim() }.sortedBy { it }.toList()
                    .toTypedArray()
            extension.pumlTableNames = (findProperty(project, "puml.tableNames")
                ?: "").split(",").asSequence().filter { it.isNotBlank() }.map { it.trim() }
                .sortedBy { it }.toList()
                .toTypedArray()

            extension.generators = (findProperty(project, "generators")
                ?: "").split(",").asSequence().filter { it.isNotBlank() }
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
        if (extension.singleDatasource) {
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
                print(Generators.tableNames(extension).joinToString(","))
            }
        }

        project.tasks.create("toPuml") { task ->
            task.group = taskGroup
            task.doLast { _ ->
                var pumlTableNames = extension.pumlTableNames
                val tables: List<Table>
                when (extension.dataType) {
                    DataType.PDM -> {
                        tables = PdmReader.read(extension.file(extension.pdmSrc))
                        if (pumlTableNames.isEmpty()) {
                            pumlTableNames = tables.map { it.tableName }.toTypedArray()
                        }
                        println("tableNames:${pumlTableNames.joinToString()}")

                    }
                    else -> {
                        if (pumlTableNames.isEmpty()) {
                            extension.use {
                                pumlTableNames = tableNames().toTypedArray()
                            }
                        }
                        tables = extension.tables(pumlTableNames)
                    }
                }
                if (tables.isNotEmpty()) {
                    val plantUML = PlantUML(
                        tables[0].moduleName,
                        extension.file(extension.pumlDatabase).absolutePath + "/database.puml",
                        null
                    )
                    plantUML.setUp(extension)
                    pumlTableNames.forEach { tableName ->
                        val table = tables.find { it.tableName.equals(tableName, true) }
                        if (table != null) {
                            plantUML.call(table)
                        }
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
            val out = File(
                extension.file(extension.pumlSrc).parent,
                extension.pumlDiagramFormat.toLowerCase()
            )
            if (src.exists())
                task.inputs.dir(src)
            if (out.exists())
                task.outputs.dir(out)
            task.doLast { _ ->
                extension.pumlSrcSources.forEach {
                    val sourceFileReader = SourceFileReader(it, out, "UTF-8")
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
        project.tasks.create("toDDL") { task ->
            task.group = taskGroup
            task.inputs.files(
                File(project.gradle.gradleUserHomeDir, "gradle.properties"),
                project.rootProject.file("gradle.properties")
            )
            val src = extension.file(extension.pumlSrc)
            val pdm = extension.file(extension.pdmSrc)
            val out = extension.file(extension.sqlDDLOutput)
            if (src.exists())
                task.inputs.dir(src)
            if (pdm.exists())
                task.inputs.file(pdm)
            if (out.exists())
                task.outputs.dir(out)
            task.doLast {
                PumlConverter.toDDL(extension)
            }
        }
        project.tasks.create("toDDLUpdate") { task ->
            task.group = taskGroup
            task.doLast {
                PumlConverter.toDDLUpdate(extension)
            }
        }
        project.tasks.create("pumlBuild") {
            it.group = taskGroup
            it.dependsOn("toDDLUpdate", "toDDL", "pumlToDiagram")
        }
    }

    private fun findProperty(project: Project, key: String) =
        (project.findProperty("generator.${project.name}.$key") as? String
            ?: project.findProperty("generator.$key") as? String)


    private fun findDatasourceProperty(project: Project, key: String): String? {
        return project.findProperty("generator.${project.name}.datasource.$key") as? String
            ?: project.findProperty("generator.datasource.$key") as? String
            ?: project.findProperty("datasource.${project.name}.$key") as? String
            ?: project.findProperty("datasource.$key") as? String
    }

}

fun GeneratorExtension.datasource(closure: Action<JDBCConnectionConfiguration>) {
    closure.execute(this.datasource)
}