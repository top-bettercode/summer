package cn.bestwu.gradle.generator

import cn.bestwu.generator.DataType
import cn.bestwu.generator.DatabaseDriver
import cn.bestwu.generator.GeneratorExtension
import cn.bestwu.generator.JDBCConnectionConfiguration
import cn.bestwu.generator.database.domain.Table
import cn.bestwu.generator.dsl.Generator
import cn.bestwu.generator.dsl.Generators
import cn.bestwu.generator.dsl.def.PlantUML
import cn.bestwu.generator.powerdesigner.PdmReader
import cn.bestwu.generator.puml.PumlConverter
import net.sourceforge.plantuml.FileFormat
import net.sourceforge.plantuml.FileFormatOption
import net.sourceforge.plantuml.SourceFileReader
import org.gradle.api.Action
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.plugins.JavaPluginConvention
import org.gradle.api.tasks.SourceSet
import java.io.File


/**
 *
 * 注册task
 *
 * @author Peter Wu
 */
class GeneratorPlugin : Plugin<Project> {

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
            })
            extension.singleDatasource = (findProperty(project, "singleDatasource"))?.toBoolean()
                    ?: true
            extension.delete = (findProperty(project, "delete"))?.toBoolean() ?: false
            extension.debug = (findProperty(project, "debug"))?.toBoolean() ?: false
            extension.projectPackage = (findProperty(project, "project-package"))?.toBoolean()
                    ?: false
            extension.deleteTablesWhenUpdate = (findProperty(project, "deleteTablesWhenUpdate"))?.toBoolean()
                    ?: false
            extension.replaceAll = (findProperty(project, "replaceAll"))?.toBoolean() ?: false
            extension.useForeignKey = (findProperty(project, "useForeignKey"))?.toBoolean() ?: false
            extension.sqlQuote = (findProperty(project, "sqlQuote"))?.toBoolean() ?: true
            extension.rootPath = project.rootProject.file("./")
            extension.basePath = project.file("./")
            extension.dir = findProperty(project, "dir") ?: "src/main/java"
            extension.packageName = findProperty(project, "packageName")
                    ?: project.findProperty("app.packageName") as? String ?: ""
            extension.userModule = (findProperty(project, "userModule"))?.toBoolean() ?: true
            extension.module = findProperty(project, "module") ?: ""
            extension.moduleName = findProperty(project, "moduleName") ?: ""
            extension.applicationName = project.findProperty("application.name") as? String
                    ?: project.rootProject.name
            extension.projectName = findProperty(project, "projectName") ?: project.name
            extension.primaryKeyName = findProperty(project, "primaryKeyName") ?: "id"
            extension.tablePrefix = findProperty(project, "tablePrefix") ?: ""
            extension.remarks = findProperty(project, "remarks") ?: ""
            extension.softDeleteColumnName = findProperty(project, "softDeleteColumnName")
                    ?: "deleted"
            extension.softDeleteAsBoolean = (findProperty(project, "softDeleteAsBoolean"))?.toBoolean()
                    ?: true
            extension.dataType = DataType.valueOf((findProperty(project, "dataType")
                    ?: DataType.DATABASE.name).toUpperCase())
            extension.updateFromType = DataType.valueOf((findProperty(project, "updateFromType")
                    ?: DataType.DATABASE.name).toUpperCase())
            //puml
            extension.pumlSrc = findProperty(project, "puml.src") ?: "puml/src"
            val pumlDatabaseDriver = findProperty(project, "puml.databaseDriver")
                    ?: extension.datasource.databaseDriver.id
            extension.pumlDatabaseDriver = DatabaseDriver.fromProductName(pumlDatabaseDriver)
            extension.pumlDatabase = findProperty(project, "puml.database") ?: "puml/database"
            extension.pumlDiagramFormat = findProperty(project, "puml.diagramFormat") ?: "PNG"
            extension.sqlOutput = findProperty(project, "sqlOutput") ?: "database"

            val settings = mutableMapOf<String, Any?>()
            project.properties.forEach { (t, any) ->
                if (t.startsWith("generator.settings"))
                    settings[t.substringAfter("generator.settings.")] = any
            }
            extension.settings = settings

            extension.tableNames = (findProperty(project, "tableNames")
                    ?: "").split(",").asSequence().filter { it.isNotBlank() }.map { it.trim() }.toList().toTypedArray()
            extension.jsonViewIgnoredFieldNames = (findProperty(project, "jsonViewIgnoredFieldNames")
                    ?: "deleted,lastModifiedDate").split(",").asSequence().filter { it.isNotBlank() }.map { it.trim() }.toList().toTypedArray()
            extension.pumlTableNames = (findProperty(project, "puml.tableNames")
                    ?: "").split(",").asSequence().filter { it.isNotBlank() }.map { it.trim() }.toList().toTypedArray()

            extension.generators = (findProperty(project, "generators")
                    ?: "").split(",").asSequence().filter { it.isNotBlank() }.map { Class.forName(it.trim()).newInstance() as Generator }.toList().toTypedArray()
        }

        project.tasks.create("generate") { task ->
            task.group = "gen"
            task.doLast {
                Generators.call(project.extensions.getByType(GeneratorExtension::class.java))
            }
        }

        project.tasks.create("packageInfo") { task ->
            task.group = "gen"
            task.doLast { _ ->
                project.convention.getPlugin(JavaPluginConvention::class.java).sourceSets.getByName(SourceSet.MAIN_SOURCE_SET_NAME).java.srcDirs.forEach { file ->
                    val srcPath = file.absolutePath + File.separator
                    file.walkTopDown().filter { it.isDirectory }.forEach { file1 ->
                        val packageInfo = File(file1, "package-info.java")
                        if (!packageInfo.exists() && file1.listFiles()?.any { it.isFile } == true) {
                            packageInfo.writeText("""package ${file1.absolutePath.replace(srcPath, "").replace(File.separator, ".")};""")
                        }
                    }
                }
            }
        }


        project.afterEvaluate {
            val extension = project.extensions.getByType(GeneratorExtension::class.java)
            if (extension.singleDatasource) {
                if (!project.rootProject.tasks.names.contains("tableNames"))
                    configPuml(project.rootProject, extension)
            } else {
                configPuml(project, extension)
            }
        }
    }

    private fun configPuml(project: Project, extension: GeneratorExtension) {
        project.tasks.create("tableNames") { task ->
            task.group = "gen"
            task.doLast {
                print(Generators.tableNames(extension).joinToString(","))
            }
        }

        project.tasks.create("toPuml") { task ->
            task.group = "gen"
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
                    val plantUML = PlantUML(tables[0].moduleName, extension.file(extension.pumlDatabase).absolutePath + "/database.puml")
                    plantUML.setUp()
                    pumlTableNames.forEach { tableName ->
                        val table = tables.find { it.tableName == tableName }
                        if (table != null) {
                            plantUML.call(extension, table)
                        }
                    }
                    plantUML.tearDown()
                }
            }
        }
        project.tasks.create("pumlReformat") { task ->
            task.group = "gen"
            task.doLast {
                PumlConverter.reformat(extension)
            }
        }
        project.tasks.create("pumlToDiagram") { task ->
            task.group = "gen"
            val src = extension.file(extension.pumlSrc)
            val out = File(extension.file(extension.pumlSrc).parent, extension.pumlDiagramFormat.toLowerCase())
            if (src.exists())
                task.inputs.dir(src)
            if (out.exists())
                task.outputs.dir(out)
            task.doLast { _ ->
                extension.pumlSrcSources.forEach {
                    val sourceFileReader = SourceFileReader(it, out, "UTF-8")
                    sourceFileReader.setFileFormatOption(FileFormatOption(FileFormat.valueOf(extension.pumlDiagramFormat)))
                    try {
                        sourceFileReader.generatedImages
                    } catch (e: Exception) {
                        println("${e.javaClass.name}:${e.message}")
                    }
                }
            }
        }
        project.tasks.create("toDDL") { task ->
            task.group = "gen"
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
            task.group = "gen"
            task.doLast {
                PumlConverter.toDDLUpdate(extension)
            }
        }
        project.tasks.create("pumlBuild") {
            it.group = "gen"
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