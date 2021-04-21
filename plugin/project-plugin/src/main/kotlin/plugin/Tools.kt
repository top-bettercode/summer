import cn.bestwu.generator.GeneratorExtension
import cn.bestwu.generator.dom.java.JavaType
import cn.bestwu.generator.dom.java.element.InnerInterface
import cn.bestwu.generator.dom.java.element.Interface
import cn.bestwu.generator.dom.java.element.JavaVisibility
import cn.bestwu.generator.dom.java.element.TopLevelClass
import cn.bestwu.generator.dsl.Generators
import hudson.cli.CLI
import org.atteo.evo.inflector.English
import org.gradle.api.Plugin
import org.gradle.api.Project
import java.io.File
import java.util.*

/**
 *
 * @author Peter Wu
 */
class Tools : Plugin<Project> {

    override fun apply(project: Project) {
        project.tasks.apply {
            val jenkinsJobs = project.property("jenkins.jobs")?.toString()?.split(",")
            val jenkinsServer = project.property("jenkins.server")?.toString()
            val jenkinsAuth = project.property("jenkins.auth")?.toString()
            if (!jenkinsJobs.isNullOrEmpty() && !jenkinsAuth.isNullOrBlank() && !jenkinsServer.isNullOrBlank()) {
                create("jenkins[All]") {
                    it.group = "tool"
                    it.doLast {
                        jenkinsJobs.forEach { jobName ->
                            CLI._main(
                                arrayOf(
                                    "-s",
                                    jenkinsServer,
                                    "-auth",
                                    jenkinsAuth,
                                    "build",
                                    jobName,
                                    "-s",
                                    "-v"
                                )
                            )
                        }
                    }
                }
                jenkinsJobs.forEach { jobName ->
                    val jobTaskName = jobName.replace(
                        "[()\\[\\]{}|/]|\\s*|\t|\r|\n|".toRegex(),
                        ""
                    )
                    create("jenkins[$jobTaskName]") {
                        it.group = "tool"
                        it.doLast {
                            CLI._main(
                                arrayOf(
                                    "-s",
                                    jenkinsServer,
                                    "-auth",
                                    jenkinsAuth,
                                    "build",
                                    jobName,
                                    "-s",
                                    "-v"
                                )
                            )
                        }
                    }
                }
            }

            create("dbMerge") {
                it.group = "tool"
                it.doLast {
                    val destFile: File = project.rootProject.file("database/init.sql")
                    val initBuilder = StringBuilder()
                    initBuilder.appendLine("SET NAMES 'utf8';")
//                    initBuilder.appendLine(project.rootProject.file("database/database.sql").readText())
                    project.rootProject.file("database/ddl").listFiles()?.filter { it.isFile }
                        ?.forEach {
                            initBuilder.appendLine(it.readText())
                        }
                    project.rootProject.file("database/init").listFiles()?.filter { it.isFile }
                        ?.forEach {
                            initBuilder.appendLine(it.readText())
                        }
                    destFile.writeText(initBuilder.toString())
                }
            }
        }
        project.plugins.apply("org.jetbrains.kotlin.jvm")
        project.subprojects {
            val subProject = it
            subProject.plugins.apply("org.jetbrains.kotlin.jvm")
            if (subProject.name == project.findProperty("tools.project") ?: "core") {
                subProject.tasks.apply {
                    create("genSerializationViews") {
                        it.group = "gen"
                        it.doLast {
                            val gen =
                                subProject.extensions.getByType(GeneratorExtension::class.java)
                            val tableNames =
                                (Generators.tableNames(gen) + gen.tableNames).distinct()
                            val type =
                                JavaType("${if (gen.projectPackage) "${gen.packageName}.${gen.projectName}" else gen.packageName}.web.CoreSerializationViews")
                            val serializationViews = Interface(type).apply {
                                javadoc {
                                    +"/**"
                                    +" * 模型属性 json SerializationViews"
                                    +" */"
                                }
                                this.visibility = JavaVisibility.PUBLIC
                                tableNames.forEach {
                                    val pathName = English.plural(gen.className(it))
                                    innerInterface(InnerInterface(JavaType("Get${pathName}List")))
                                    innerInterface(InnerInterface(JavaType("Get${pathName}Info")))
                                }
                            }
                            subProject.file(
                                "src/main/java/${
                                    type.fullyQualifiedName.replace(
                                        '.',
                                        '/'
                                    )
                                }.java"
                            ).writeText(serializationViews.formattedContent)
                        }
                    }
                    create("printMapper") {
                        it.group = "gen"
                        it.doLast {
                            val gen =
                                subProject.extensions.getByType(GeneratorExtension::class.java)
                            gen.generators = arrayOf(MapperPrint())
                            Generators.call(gen)
                        }
                    }
                    create("printMybatisWhere") {
                        it.group = "gen"
                        it.doLast {
                            val gen =
                                subProject.extensions.getByType(GeneratorExtension::class.java)
                            gen.generators = arrayOf(MybatisWherePrint())
                            Generators.call(gen)
                        }
                    }

                    create("printSetter") {
                        it.group = "gen"
                        it.doLast {
                            val gen =
                                subProject.extensions.getByType(GeneratorExtension::class.java)
                            gen.generators = arrayOf(SetterPrint(false))
                            Generators.call(gen)
                        }
                    }

                    create("printSetter2") {
                        it.group = "gen"
                        it.doLast {
                            val gen =
                                subProject.extensions.getByType(GeneratorExtension::class.java)
                            gen.generators = arrayOf(SetterPrint(true))
                            Generators.call(gen)
                        }
                    }


                    create("printExcelField") {
                        it.group = "gen"
                        it.doLast {
                            val gen =
                                subProject.extensions.getByType(GeneratorExtension::class.java)
                            gen.generators = arrayOf(ExcelFieldPrint())
                            Generators.call(gen)
                        }
                    }
                    create("dbDoc") {
                        it.group = "tool"
                        it.doLast {
                            val dbDoc = DbDoc(subProject)
                            val gen =
                                subProject.extensions.getByType(GeneratorExtension::class.java)
                            gen.generators = arrayOf(dbDoc)
                            gen.tableNames = arrayOf()
                            Generators.call(gen)
                        }
                    }
                    create("dicCode") {
                        it.group = "tool"
                        it.doLast {
                            val propertiesFile =
                                subProject.file("src/main/resources/default-dic-code.properties")
                            propertiesFile.parentFile.mkdirs()
                            propertiesFile.writeText("")
                            val codeTypes: MutableSet<String> = mutableSetOf()
                            //生成 properties
                            val gen =
                                subProject.extensions.getByType(GeneratorExtension::class.java)
                            gen.generators =
                                arrayOf(DicCodeProperties(propertiesFile, codeTypes))
                            gen.tableNames = arrayOf()
                            Generators.call(gen)
                            //生成
                            val dicCodeGen = DicCodeGen(subProject)
                            dicCodeGen.setUp()
                            dicCodeGen.genCode()
                            dicCodeGen.tearDown()
                        }
                    }
                    create("genErrorCode") {
                        it.group = "tool"
                        it.doLast {
                            val file = subProject.file("src/main/resources/error-code.properties")
                            if (file.exists()) {
                                val gen =
                                    subProject.extensions.getByType(GeneratorExtension::class.java)
                                val destFile = subProject.file(
                                    "src/main/java/${
                                        gen.packageName.replace(
                                            '.',
                                            '/'
                                        )
                                    }/support/ErrorCode.java"
                                )
                                val clazz =
                                    TopLevelClass(JavaType("${gen.packageName}.support.ErrorCode"))

                                clazz.visibility = JavaVisibility.PUBLIC
                                clazz.apply {
                                    javadoc {
                                        +"/**"
                                        +" * 业务错误码"
                                        +" */"
                                    }
                                    val properties = Properties()
                                    properties.load(file.inputStream())
                                    properties.forEach { k, v ->
                                        field(
                                            "CODE_$k",
                                            JavaType.stringInstance,
                                            "\"$k\"",
                                            true,
                                            JavaVisibility.PUBLIC
                                        ) {
                                            isStatic = true
                                            javadoc {
                                                +"/**"
                                                +" * $v"
                                                +" */"
                                            }
                                        }
                                    }
                                    subProject.rootProject.file("doc/业务相关错误码.adoc").printWriter()
                                        .use {
                                            it.println(
                                                """
== 业务相关错误码

当处理建议为空时，取 message 字段内容提示用户即可。

|===
| 业务相关错误码 | 处理建议 | 说明 |
                            """.trimIndent()
                                            )
                                            properties.forEach { k, v ->
                                                it.println("| $k | $v |  |")
                                            }
                                            it.println("|===")
                                        }
                                }
                                destFile.writeText(clazz.formattedContent)
                            }
                        }
                    }
                }
            }
        }
    }
}