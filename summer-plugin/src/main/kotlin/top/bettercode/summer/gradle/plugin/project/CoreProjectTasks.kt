package top.bettercode.summer.gradle.plugin.project

import isCore
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
import top.bettercode.summer.tools.lang.capitalized
import java.util.*


/**
 *
 * @author Peter Wu
 */
@Suppress("ObjectLiteralToLambda")
object CoreProjectTasks {

    fun config(project: Project) {

        project.tasks.apply {

            val ext = project.extensions.getByType(GeneratorExtension::class.java)
            val prefix = "Core"
            val group = "gen $prefix code"

            if (ext.hasPuml && project.isCore) {
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
            }

            create("printMapper") {
                it.group = GeneratorPlugin.PRINT_GROUP
                it.doLast(object : Action<Task> {
                    override fun execute(it: Task) {
                        val gen = project.extensions.getByType(GeneratorExtension::class.java)
                        gen.generators = arrayOf(MapperPrint())
                        Generators.callInAllModule(gen)
                    }
                })
            }
            create("printMybatisWhere") {
                it.group = GeneratorPlugin.PRINT_GROUP
                it.doLast(object : Action<Task> {
                    override fun execute(it: Task) {
                        val gen = project.extensions.getByType(GeneratorExtension::class.java)
                        gen.generators = arrayOf(MybatisWherePrint())
                        Generators.callInAllModule(gen)
                    }
                })
            }

            create("printSetter") {
                it.group = GeneratorPlugin.PRINT_GROUP
                it.doLast(object : Action<Task> {
                    override fun execute(it: Task) {
                        val gen = project.extensions.getByType(GeneratorExtension::class.java)
                        gen.generators = arrayOf(SetterPrint(true))
                        Generators.callInAllModule(gen)
                    }
                })
            }

            create("printExcelCode") {
                it.group = GeneratorPlugin.PRINT_GROUP
                it.doLast(object : Action<Task> {
                    override fun execute(it: Task) {
                        val gen = project.extensions.getByType(GeneratorExtension::class.java)
                        gen.generators = arrayOf(ExcelCodePrint())
                        Generators.callInAllModule(gen)
                    }
                })
            }
            create("printOldExcelCode") {
                it.group = GeneratorPlugin.PRINT_GROUP
                it.doLast(object : Action<Task> {
                    override fun execute(task: Task) {
                        project.rootDir.walkTopDown().filter { file -> file.isFile && file.name.endsWith(".java") && file.readText().contains("@ExcelField") }.forEach { file ->
                            val className = file.nameWithoutExtension
                            val codes = mutableMapOf<Int, String>()
                            val readLines = file.readLines()
                            for (i in readLines.indices) {
                                val line = readLines[i].trim()
                                //如果是以@ExcelField开头
                                if (line.startsWith("@ExcelField")) {
                                    //提取@ExcelField(title = "商品分类名称", sort = 1)中的"商品分类名称"字符
                                    val title = line.substringAfter("title = \"").substringBefore("\"")
                                    //sort = 1
                                    val sort = line.substringAfter("sort = ", "0").substringBefore(")").substringBefore(",").trim().toInt()
                                    // 是否  YuanConverter
                                    val isYuanConverter = line.contains("YuanConverter")
                                    // 是否  MoneyToConverter
                                    val isMoneyToConverter = line.contains("MoneyToConverter")
                                    // 是否 CodeConverter
                                    val isCodeConverter = line.contains("CodeConverter")
                                    // 是否  WeightToConverter
                                    val isWeightToConverter = line.contains("WeightToConverter")
                                    //DateConverter.class, pattern = "yyyy-MM-dd HH:mm:ss"
                                    val isDateConverter = line.contains("DateConverter")
                                    val format = line.substringAfter("pattern = \"").substringBefore("\"").trim().lowercase(Locale.getDefault())
                                    // 是否  Converter
                                    val isConverter = line.contains("converter")
                                    // converter = WeightToConverter.class
                                    val converter = line.substringAfter("converter = ").substringBefore(")")

                                    val readName = findReadName(readLines, i)
                                    //ExcelField.of("商品分类名称", OrderReceivablesCusto::getCommoTyName),
                                    codes[sort] = ("""
                    ExcelField.of("$title", $className::${readName})${
                                        if (isYuanConverter || isMoneyToConverter) {
                                            ".yuan()"
                                        } else if (isCodeConverter) {
                                            ".code()"
                                        } else if (isWeightToConverter) {
                                            ".unit(1000, 3)"
                                        } else if (isDateConverter) {
                                            ".format(\"$format\")"
                                        } else if (isConverter) {
                                            ".converter(${converter})"
                                        } else {
                                            ""
                                        }
                                    },
                    """.trimIndent())

                                }
                            }

                            if (codes.isNotEmpty()) {
                                project.logger.lifecycle("======================================")
                                var code = "private final ExcelField<$className, ?>[] excelFields = ArrayUtil.of(\n"
                                codes.keys.sorted().forEach { c ->
                                    code += codes[c] + "\n"
                                }
                                code += ");"
                                project.logger.lifecycle(code)
                            }
                        }
                        project.logger.lifecycle("======================================")
                    }
                })
            }
            create("genDbDoc") {
                it.group = GeneratorPlugin.GEN_GROUP
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
                it.group = GeneratorPlugin.GEN_GROUP
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
                t.group = GeneratorPlugin.GEN_GROUP
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

    private fun findReadName(lines: List<String>, i: Int): String {
        return if (i + 1 < lines.size) {
            val readName = lines[i + 1].trim()
            //如果符合private String commoTyName;返回，如果不符合继续查找
            if (readName.startsWith("private ")) {
                "get${readName.replace("private \\S* (.*?);.*".toRegex(), "$1").trim().capitalized()}"
            } else if (readName.startsWith("public ")) {
                //public String getBrandName()
                readName.replace("public \\S*? (.*?)\\(.*".toRegex(), "$1").trim()
            } else {
                findReadName(lines, i + 1)
            }
        } else {
            ""
        }
    }

}