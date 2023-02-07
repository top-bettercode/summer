package top.bettercode.summer.gradle.plugin.project

import org.gradle.api.Action
import org.gradle.api.Project
import org.gradle.api.Task
import top.bettercode.summer.gradle.plugin.generator.GeneratorPlugin
import top.bettercode.summer.tools.generator.GeneratorExtension
import top.bettercode.summer.tools.generator.dom.java.JavaType
import top.bettercode.summer.tools.generator.dom.java.element.InnerInterface
import top.bettercode.summer.tools.generator.dom.java.element.Interface
import top.bettercode.summer.tools.generator.dom.java.element.JavaVisibility
import top.bettercode.summer.tools.generator.dom.java.element.TopLevelClass
import top.bettercode.summer.tools.generator.dom.unit.FileUnit
import top.bettercode.summer.tools.generator.dsl.Generators
import java.util.*


/**
 *
 * @author Peter Wu
 */
object CoreProjectTasks {

    fun config(project: Project) {

        project.tasks.apply {
            create("print[Mapper]") {
                it.group = GeneratorPlugin.printGroup
                it.doLast(object : Action<Task> {
                    override fun execute(it: Task) {
                        val gen = project.extensions.getByType(GeneratorExtension::class.java)
                        gen.generators = arrayOf(MapperPrint())
                        Generators.call(gen)
                    }
                })
            }
            create("print[MybatisWhere]") {
                it.group = GeneratorPlugin.printGroup
                it.doLast(object : Action<Task> {
                    override fun execute(it: Task) {
                        val gen = project.extensions.getByType(GeneratorExtension::class.java)
                        gen.generators = arrayOf(MybatisWherePrint())
                        Generators.call(gen)
                    }
                })
            }

            create("print[Setter]") {
                it.group = GeneratorPlugin.printGroup
                it.doLast(object : Action<Task> {
                    override fun execute(it: Task) {
                        val gen = project.extensions.getByType(GeneratorExtension::class.java)
                        gen.generators = arrayOf(SetterPrint(true))
                        Generators.call(gen)
                    }
                })
            }

            create("print[ExcelCode]") {
                it.group = GeneratorPlugin.printGroup
                it.doLast(object : Action<Task> {
                    override fun execute(it: Task) {
                        val gen = project.extensions.getByType(GeneratorExtension::class.java)
                        gen.generators = arrayOf(ExcelCodePrint())
                        Generators.call(gen)
                    }
                })
            }
            create("gen[DbDoc]") {
                it.group = GeneratorPlugin.genGroup
                it.doLast(object : Action<Task> {
                    override fun execute(it: Task) {
                        val dbDoc = DbDoc(project)
                        val gen = project.extensions.getByType(GeneratorExtension::class.java)
                        gen.generators = arrayOf(dbDoc)
                        gen.tableNames = emptyArray()
                        Generators.call(gen)
                    }
                })
            }
            create("gen[DicCode]") {
                it.group = GeneratorPlugin.genGroup
                it.doLast(object : Action<Task> {
                    override fun execute(it: Task) {
                        val gen = project.extensions.getByType(GeneratorExtension::class.java)
                        //生成 properties
                        gen.tableNames = emptyArray()
                        gen.generators = arrayOf(DicCodeProperties())
                        Generators.call(gen)
                        //生成
                        DicCodeGen(project).run()
                    }
                })
            }
            create("gen[ErrorCode]") { t ->
                t.group = GeneratorPlugin.genGroup
                t.doLast(object : Action<Task> {
                    override fun execute(it: Task) {
                        val file = project.file("src/main/resources/error-code.properties")
                        if (file.exists()) {
                            val gen = project.extensions.getByType(GeneratorExtension::class.java)
                            val clazz = TopLevelClass(
                                type = JavaType("${gen.packageName}.support.ErrorCode"),
                                overwrite = true
                            )

                            val properties = Properties()
                            properties.load(file.inputStream())

                            clazz.visibility = JavaVisibility.PUBLIC
                            clazz.apply {
                                javadoc {
                                    +"/**"
                                    +" * 业务错误码"
                                    +" */"
                                }
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
                            }
                            clazz.writeTo(project.projectDir)

                            val docfile = FileUnit("doc/业务相关错误码.adoc")
                            docfile.apply {
                                +"""
== 业务相关错误码

当处理建议为空时，取 message 字段内容提示用户即可。

|===
| 业务相关错误码 | 处理建议 | 说明 |
                        """.trimIndent()
                                properties.forEach { k, v ->
                                    +"| $k | $v |  |"
                                }
                                +"|==="
                            }
                            docfile.writeTo(project.rootDir)
                        }
                    }
                })
            }
        }

    }
}