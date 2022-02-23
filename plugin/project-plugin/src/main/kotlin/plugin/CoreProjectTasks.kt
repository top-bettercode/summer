package plugin

import org.atteo.evo.inflector.English
import org.gradle.api.Project
import top.bettercode.generator.GeneratorExtension
import top.bettercode.generator.dom.java.JavaType
import top.bettercode.generator.dom.java.element.InnerInterface
import top.bettercode.generator.dom.java.element.Interface
import top.bettercode.generator.dom.java.element.JavaVisibility
import top.bettercode.generator.dom.java.element.TopLevelClass
import top.bettercode.generator.dsl.Generators
import top.bettercode.generator.puml.PumlConverter
import top.bettercode.gradle.generator.GeneratorPlugin
import java.io.File
import java.util.*


/**
 *
 * @author Peter Wu
 */
object CoreProjectTasks {

    fun config(project: Project) {

        project.tasks.apply {
            create("genSerializationViews") { t ->
                t.group = GeneratorPlugin.taskGroup
                t.doLast {
                    val gen = project.extensions.getByType(GeneratorExtension::class.java)
                    val tableNames =
                        (Generators.tableNames(gen) + gen.tableNames).sortedBy { it }.distinct()
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
                    val file = project.file(
                        "src/main/java/${
                            type.fullyQualifiedName.replace(
                                '.',
                                '/'
                            )
                        }.java"
                    )
                    if (!file.parentFile.exists()) {
                        file.parentFile.mkdirs()
                    }
                    println(
                        "${if (file.exists()) "覆盖" else "生成"}：${
                            file.absolutePath.substringAfter(
                                project.rootDir.absolutePath + File.separator
                            )
                        }"
                    )
                    file.writeText(serializationViews.formattedContent)
                }
            }
            create("printMapper") {
                it.group = GeneratorPlugin.taskGroup
                it.doLast {
                    val gen = project.extensions.getByType(GeneratorExtension::class.java)
                    gen.generators = arrayOf(MapperPrint())
                    Generators.call(gen)
                }
            }
            create("printMybatisWhere") {
                it.group = GeneratorPlugin.taskGroup
                it.doLast {
                    val gen = project.extensions.getByType(GeneratorExtension::class.java)
                    gen.generators = arrayOf(MybatisWherePrint())
                    Generators.call(gen)
                }
            }

            create("printSetter") {
                it.group = GeneratorPlugin.taskGroup
                it.doLast {
                    val gen = project.extensions.getByType(GeneratorExtension::class.java)
                    gen.generators = arrayOf(SetterPrint(true))
                    Generators.call(gen)
                }
            }

            create("printExcelField") {
                it.group = GeneratorPlugin.taskGroup
                it.doLast {
                    val gen = project.extensions.getByType(GeneratorExtension::class.java)
                    gen.generators = arrayOf(ExcelFieldPrint())
                    Generators.call(gen)
                }
            }
            create("genDbDoc") {
                it.group = GeneratorPlugin.taskGroup
                it.doLast {
                    val dbDoc = DbDoc(project)
                    val gen = project.extensions.getByType(GeneratorExtension::class.java)
                    gen.generators = arrayOf(dbDoc)
                    gen.tableNames = arrayOf()
                    Generators.call(gen)
                }
            }
            create("genDicCode") {
                it.group = GeneratorPlugin.taskGroup
                it.doLast {
                    val propertiesFile =
                        project.file("src/main/resources/default-dic-code.properties")
                    propertiesFile.parentFile.mkdirs()
                    val gen = project.extensions.getByType(GeneratorExtension::class.java)
                    println(
                        "${if (propertiesFile.exists()) "覆盖" else "生成"}：${
                            propertiesFile.absolutePath.substringAfter(
                                project.rootDir.absolutePath + File.separator
                            )
                        }"
                    )
                    propertiesFile.writeText("")
                    val codeTypes: MutableSet<String> = mutableSetOf()
                    //生成 properties
                    gen.tableNames= emptyArray()
                    gen.generators = arrayOf(DicCodeProperties(propertiesFile, codeTypes))
                    Generators.call(gen)
                    //生成
                    val dicCodeGen = DicCodeGen(project)
                    dicCodeGen.setUp()
                    dicCodeGen.genCode()
                    dicCodeGen.tearDown()
                }
            }
            create("genErrorCode") { t ->
                t.group = GeneratorPlugin.taskGroup
                t.doLast {
                    val file = project.file("src/main/resources/error-code.properties")
                    if (file.exists()) {
                        val gen = project.extensions.getByType(GeneratorExtension::class.java)
                        val destFile = project.file(
                            "src/main/java/${
                                gen.packageName.replace(
                                    '.',
                                    '/'
                                )
                            }/support/ErrorCode.java"
                        )
                        val clazz = TopLevelClass(JavaType("${gen.packageName}.support.ErrorCode"))

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
                            val docfile = project.rootProject.file("doc/业务相关错误码.adoc")
                            println(
                                "${if (docfile.exists()) "覆盖" else "生成"}：${
                                    docfile.absolutePath.substringAfter(
                                        project.rootDir.absolutePath + File.separator
                                    )
                                }"
                            )
                            docfile.printWriter()
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
                        println(
                            "${if (destFile.exists()) "覆盖" else "生成"}：${
                                destFile.absolutePath.substringAfter(
                                    project.rootDir.absolutePath + File.separator
                                )
                            }"
                        )
                        destFile.writeText(clazz.formattedContent)
                    }
                }
            }
        }

    }
}