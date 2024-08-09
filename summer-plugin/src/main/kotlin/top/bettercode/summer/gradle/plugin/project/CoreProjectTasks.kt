package top.bettercode.summer.gradle.plugin.project

import com.fasterxml.jackson.databind.JsonNode
import com.github.stuxuhai.jpinyin.PinyinFormat
import com.github.stuxuhai.jpinyin.PinyinHelper
import isCloud
import isCore
import org.gradle.api.Action
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.UnknownProjectException
import top.bettercode.summer.gradle.plugin.generator.GeneratorPlugin
import top.bettercode.summer.gradle.plugin.project.template.*
import top.bettercode.summer.tools.generator.GeneratorExtension
import top.bettercode.summer.tools.generator.dom.java.element.Interface
import top.bettercode.summer.tools.generator.dom.java.element.JavaVisibility
import top.bettercode.summer.tools.generator.dom.java.element.TopLevelClass
import top.bettercode.summer.tools.generator.dom.unit.FileUnit
import top.bettercode.summer.tools.generator.dsl.DicCodes
import top.bettercode.summer.tools.generator.dsl.Generators
import top.bettercode.summer.tools.lang.capitalized
import top.bettercode.summer.tools.lang.util.JavaType
import top.bettercode.summer.tools.lang.util.StringUtil
import top.bettercode.summer.tools.lang.util.StringUtil.toCamelCase
import java.io.File
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

            if (project.isCore) {
                create("genDicCode") {
                    it.group = GeneratorPlugin.GEN_GROUP
                    it.doLast(object : Action<Task> {
                        override fun execute(it: Task) {
                            if (project.isCloud) {
                                val pumlSources: MutableMap<String, MutableList<File>> =
                                    mutableMapOf()
                                project.rootProject.subprojects.forEach { sub ->
                                    val gen =
                                        sub.extensions.getByType(GeneratorExtension::class.java)
                                    gen.pumlSources.forEach { (module, sources) ->
                                        val files = pumlSources.computeIfAbsent(module) {
                                            sources.toMutableList()
                                        }
                                        files.addAll(sources)
                                    }
                                }
                                ext.tableNames = emptyArray()
                                ext.generators = arrayOf(DicCodeProperties())
                                Generators.callInAllModule(
                                    extension = ext,
                                    pumlSources = pumlSources
                                )
                            } else {
                                //生成 properties
                                ext.tableNames = emptyArray()
                                ext.generators = arrayOf(DicCodeProperties())
                                Generators.callInAllModule(ext)
                            }
                            //生成
                            DicCodeGen(project, ext.packageName).run()
                        }
                    })
                }

                if (project.isCloud)
                    create("genMsg") {
                        it.group = GeneratorPlugin.GEN_GROUP
                        it.doLast(object : Action<Task> {
                            override fun execute(it: Task) {
                                val pumlSources: MutableMap<String, MutableList<File>> =
                                    mutableMapOf()
                                project.rootProject.subprojects.forEach { sub ->
                                    val gen =
                                        sub.extensions.getByType(GeneratorExtension::class.java)
                                    gen.pumlSources.forEach { (module, sources) ->
                                        val files = pumlSources.computeIfAbsent(module) {
                                            sources.toMutableList()
                                        }
                                        files.addAll(sources)
                                    }
                                }
                                ext.tableNames = emptyArray()
                                ext.generators = arrayOf(Msg())
                                Generators.callInAllModule(
                                    extension = ext,
                                    pumlSources = pumlSources
                                )
                            }
                        })
                    }

            }

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
                        ext.generators = arrayOf(MapperPrint())
                        Generators.callInAllModule(ext)
                    }
                })
            }
            create("printMybatisWhere") {
                it.group = GeneratorPlugin.PRINT_GROUP
                it.doLast(object : Action<Task> {
                    override fun execute(it: Task) {
                        ext.generators = arrayOf(MybatisWherePrint())
                        Generators.callInAllModule(ext)
                    }
                })
            }

            create("printSetter") {
                it.group = GeneratorPlugin.PRINT_GROUP
                it.doLast(object : Action<Task> {
                    override fun execute(it: Task) {
                        ext.generators = arrayOf(SetterPrint(true))
                        Generators.callInAllModule(ext)
                    }
                })
            }

            create("genDbDoc") {
                it.group = GeneratorPlugin.GEN_GROUP
                it.doLast(object : Action<Task> {
                    override fun execute(it: Task) {
                        val dbDoc = DbDoc(project)
                        ext.generators = arrayOf(dbDoc)
                        ext.tableNames = emptyArray()
                        Generators.callInAllModule(ext)
                    }
                })
            }

            create("updateCode") {
                it.group = GeneratorPlugin.GEN_GROUP
                it.doLast(object : Action<Task> {
                    override fun execute(t: Task) {
                        val doclog = StringBuilder()
                        project.rootProject.subprojects { subproject ->
                            subproject.file("src/doc").walkTopDown()
                                .filter { f -> f.isFile && f.extension == "yml" }
                                .forEach { f ->
                                    val lines = f.readLines()
                                    val isGet = lines.any { l ->
                                        return@any l.contains("method: \"GET\"")
                                    }
                                    if (isGet) {
                                        val newLines = lines.map { l ->
                                            if (l.trim() == "parametersExt:") {
                                                doclog.append(".")
                                                "  queriesExt:"
                                            } else {
                                                l
                                            }
                                        }
                                        f.writeText(newLines.joinToString("\n") + "\n")
                                    }
                                }
                            subproject.logger.lifecycle(doclog.toString())
                            subproject.file("src/test/java").walkTopDown()
                                .filter { f -> f.isFile && (f.extension == "java" || f.extension == "kt") }
                                .forEach { f ->
                                    val lines = f.readLines()
                                    var isGet = false
                                    var update = false
                                    val log = StringBuilder()
                                    val newLines = lines.map { l ->
                                        if (l.matches(Regex(".*get\\(.*"))) {
                                            isGet = true
                                            l
                                        } else if (l.trim().startsWith(".param(") && isGet) {
                                            update = true
                                            log.append(".")
                                            l.replace(".param(", ".queryParam(")
                                        } else {
                                            if (isGet && l.contains(");")) {
                                                isGet = false
                                            }
                                            l
                                        }
                                    }
                                    if (update) {
                                        f.writeText(newLines.joinToString("\n") + "\n")
                                        subproject.logger.lifecycle(log.toString())
                                    }
                                }
                        }
                    }
                })
            }

            create("genAuthCode") {
                it.group = GeneratorPlugin.GEN_GROUP
                it.doLast(object : Action<Task> {
                    override fun execute(t: Task) {
                        val file = project.rootProject.file("conf/auth.json")
                        val replaceCodeNames: MutableMap<String, String> = mutableMapOf()
                        val authProject = try {
                            project.rootProject.project(
                                project.findProperty("app.authProject")?.toString()
                                    ?: "admin"
                            )
                        } catch (e: UnknownProjectException) {
                            try {
                                project.rootProject.project("app")
                            } catch (e: UnknownProjectException) {
                                project
                            }
                        }
                        val codeGen = DicCodeGen(project, ext.packageName)
                        if (file.exists()) {
                            val map = StringUtil.readJsonTree(file.readText())
                            project.logger.lifecycle("======================================")
                            project.logger.lifecycle(
                                "#auth\n" +
                                        "auth=权限\n" +
                                        "auth|TYPE=String"
                            )
                            val dicCodes = DicCodes(
                                type = "auth",
                                name = "权限",
                                javaType = JavaType.stringInstance,
                            )
                            map.forEach { node ->
                                printNode(project, dicCodes, node)
                            }
                            codeGen.genCode(dicCodes = dicCodes, auth = true)
                            project.logger.lifecycle("======================================")
                            map.forEach { node ->
                                genNode(authProject, codeGen, node, replaceCodeNames)
                            }
                        }
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

    private fun printNode(project: Project, dicCodes: DicCodes, node: JsonNode) {
        val code = node.get("id").asText()
        val name = node.get("name").asText()
        project.logger.lifecycle("auth.$code=$name")
        dicCodes.codes[code] = name
        val jsonNode = node.get("children")
        if (!jsonNode.isEmpty) {
            jsonNode.forEach { printNode(project, dicCodes, it) }
        }
    }

    private fun genNode(
        project: Project,
        codeGen: DicCodeGen,
        node: JsonNode,
        replaceCodeNames: MutableMap<String, String>
    ) {
        genCode(
            project,
            codeGen,
            replaceCodeNames,
            node.get("id").asText(),
            node.get("name").asText()
        )
        val jsonNode = node.get("children")
        if (!jsonNode.isEmpty) {
            jsonNode.forEach { genNode(project, codeGen, it, replaceCodeNames) }
        }
    }

    private fun genCode(
        project: Project,
        codeGen: DicCodeGen,
        replaceCodeNames: MutableMap<String, String>,
        code: String,
        name: String
    ) {
        val oldAuthName = "Auth${
            PinyinHelper.convertToPinyinString(
                name,
                "_",
                PinyinFormat.WITHOUT_TONE
            ).split('_').joinToString("") {
                it.capitalized()
            }
        }"

        val authClassName =
            "Auth${codeGen.codeName(code, name).toCamelCase(true)}"
        replaceCodeNames[oldAuthName] = authClassName
        val packageName = project.property("app.packageName") as String
        Interface(
            type = JavaType("$packageName.security.auth.$authClassName"),
            overwrite = true
        ).apply {
            isAnnotation = true
            javadoc {
                +"/**"
                +" * $name 权限标识"
                +" *"
                +" */"
            }

            import("java.lang.annotation.ElementType")
            annotation("@java.lang.annotation.Target({ElementType.METHOD, ElementType.TYPE})")

            import("java.lang.annotation.RetentionPolicy")
            annotation("@java.lang.annotation.Retention(RetentionPolicy.RUNTIME)")
            annotation("@java.lang.annotation.Inherited")
            annotation("@java.lang.annotation.Documented")

            val codeFieldName = codeGen.codeName(code, name)
            import("$packageName.security.auth.AuthEnum.AuthConst")
            annotation("@top.bettercode.summer.security.authorize.ConfigAuthority(AuthConst.$codeFieldName)")

        }.writeTo(project.projectDir)

    }

    private fun findReadName(lines: List<String>, i: Int): String {
        return if (i + 1 < lines.size) {
            val readName = lines[i + 1].trim()
            //如果符合private String commoTyName;返回，如果不符合继续查找
            if (readName.startsWith("private ")) {
                "get${
                    readName.replace("private \\S* (.*?);.*".toRegex(), "$1").trim().capitalized()
                }"
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