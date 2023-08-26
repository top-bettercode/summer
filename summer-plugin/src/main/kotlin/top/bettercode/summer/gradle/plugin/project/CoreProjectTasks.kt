package top.bettercode.summer.gradle.plugin.project

import org.gradle.api.Action
import org.gradle.api.Project
import org.gradle.api.Task
import top.bettercode.summer.gradle.plugin.generator.GeneratorPlugin
import top.bettercode.summer.gradle.plugin.project.template.*
import top.bettercode.summer.tools.generator.GeneratorExtension
import top.bettercode.summer.tools.generator.dom.java.JavaType
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

            val ext = project.extensions.getByType(GeneratorExtension::class.java)
            val prefix = "Core"
            val group = "gen $prefix code"

            project.tasks.create("gen${prefix}Entity") { task ->
                task.group = group
                task.doLast(object : Action<Task> {
                    override fun execute(it: Task) {
                        ext.tableNames = emptyArray()
                        ext.generators = arrayOf(Entity(), SerializationViews())
                        Generators.callInAllModule(ext)
                    }
                })
            }

            project.tasks.create("gen${prefix}Form") { task ->
                task.group = group
                task.doLast(object : Action<Task> {
                    override fun execute(it: Task) {
                        ext.generators = arrayOf(Form())
                        Generators.callInAllModule(ext)
                    }
                })
            }

            project.tasks.create("gen${prefix}DO") { task ->
                task.group = group
                task.doLast(object : Action<Task> {
                    override fun execute(it: Task) {
                        ext.generators = arrayOf(DataObject())
                        Generators.callInAllModule(ext)
                    }
                })
            }

            project.tasks.create("gen${prefix}Service") { task ->
                task.group = group
                task.doLast(object : Action<Task> {
                    override fun execute(it: Task) {
                        ext.generators = arrayOf(Service())
                        Generators.callInAllModule(ext)
                    }
                })
            }
            project.tasks.create("gen${prefix}Controller") { task ->
                task.group = group
                task.doLast(object : Action<Task> {
                    override fun execute(it: Task) {
                        ext.generators = arrayOf(Form(), Controller())
                        Generators.callInAllModule(ext)
                    }
                })
            }

            create("printMapper") {
                it.group = GeneratorPlugin.printGroup
                it.doLast(object : Action<Task> {
                    override fun execute(it: Task) {
                        val gen = project.extensions.getByType(GeneratorExtension::class.java)
                        gen.generators = arrayOf(MapperPrint())
                        Generators.callInAllModule(gen)
                    }
                })
            }
            create("printMybatisWhere") {
                it.group = GeneratorPlugin.printGroup
                it.doLast(object : Action<Task> {
                    override fun execute(it: Task) {
                        val gen = project.extensions.getByType(GeneratorExtension::class.java)
                        gen.generators = arrayOf(MybatisWherePrint())
                        Generators.callInAllModule(gen)
                    }
                })
            }

            create("printSetter") {
                it.group = GeneratorPlugin.printGroup
                it.doLast(object : Action<Task> {
                    override fun execute(it: Task) {
                        val gen = project.extensions.getByType(GeneratorExtension::class.java)
                        gen.generators = arrayOf(SetterPrint(true))
                        Generators.callInAllModule(gen)
                    }
                })
            }

            create("printExcelCode") {
                it.group = GeneratorPlugin.printGroup
                it.doLast(object : Action<Task> {
                    override fun execute(it: Task) {
                        val gen = project.extensions.getByType(GeneratorExtension::class.java)
                        gen.generators = arrayOf(ExcelCodePrint())
                        Generators.callInAllModule(gen)
                    }
                })
            }
            create("genDbDoc") {
                it.group = GeneratorPlugin.genGroup
                it.doLast(object : Action<Task> {
                    override fun execute(it: Task) {
                        val dbDoc = DbDoc(project)
                        val gen = project.extensions.getByType(GeneratorExtension::class.java)
                        gen.generators = arrayOf(dbDoc)
                        gen.tableNames = emptyArray()
                        Generators.callInAllModule(gen)
                    }
                })
            }
            create("genDicCode") {
                it.group = GeneratorPlugin.genGroup
                it.doLast(object : Action<Task> {
                    override fun execute(it: Task) {
                        val gen = project.extensions.getByType(GeneratorExtension::class.java)
                        //生成 properties
                        gen.tableNames = emptyArray()
                        gen.generators = arrayOf(DicCodeProperties())
                        Generators.callInAllModule(gen)
                        //生成
                        DicCodeGen(project).run()
                    }
                })
            }
            create("genErrorCode") { t ->
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