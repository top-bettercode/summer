package top.bettercode.summer.gradle.plugin.generator

import org.gradle.api.Action
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.plugins.JavaPluginExtension
import org.gradle.api.tasks.SourceSet
import top.bettercode.summer.tools.generator.dom.java.JavaType
import top.bettercode.summer.tools.generator.dom.java.element.PackageInfo
import top.bettercode.summer.tools.generator.dom.unit.DirectorySet
import top.bettercode.summer.tools.generator.dom.unit.FileUnit
import java.io.File

/**
 *
 * @author Peter Wu
 */

class GenPackageinfoPlugin : Plugin<Project> {

    override fun apply(project: Project) {

        project.tasks.create("gen[PackageInfo]") { task ->
            task.group = GeneratorPlugin.genGroup
            task.doLast(object : Action<Task> {
                override fun execute(it: Task) {
                    project.allprojects { subProject ->
                        subProject.extensions.getByType(JavaPluginExtension::class.java).sourceSets.getByName(
                            SourceSet.MAIN_SOURCE_SET_NAME
                        ).java.srcDirs.forEach { srcDir ->
                            srcDir.walkTopDown().filter { it.isDirectory }.forEach { packageDir ->
                                val listFiles =
                                    packageDir.listFiles()
                                        ?.filter { it.name != "package-info.java" }
                                if (listFiles != null && (listFiles.count() > 1 || listFiles.any { it.isFile } || packageDir.name == "response")) {
                                    val packageinfo =
                                        packageDir.absolutePath.replace(
                                            srcDir.absolutePath + File.separator,
                                            ""
                                        )
                                            .replace(File.separator, ".")
                                    val file = File(packageDir, "package-info.java")
                                    val packageInfoFile = PackageInfo(
                                        JavaType("$packageinfo.package-info"),
                                        overwrite = file.exists() && (file.readLines().size == 1 || file.readText()
                                            .replace(
                                                """/**
 * 
 */
""", ""
                                            ).startsWith("package"))
                                    )
                                    packageInfoFile.apply {
                                        javadoc {
                                            +"/**"
                                            +" * ${defaultComment(packageinfo)}"
                                            +" */"
                                        }
                                    }
                                    packageInfoFile.writeTo(subProject.projectDir)
                                }
                            }
                        }

                        val srcDir = subProject.file("src/main/kotlin")
                        srcDir.walkTopDown().filter { it.isDirectory }.forEach { packageDir ->
                            val listFiles =
                                packageDir.listFiles()?.filter { it.name != "package-info.java" }
                            if (listFiles != null && (listFiles.count() > 1 || listFiles.any { it.isFile } || packageDir.name == "response")) {
                                val packageinfo =
                                    packageDir.absolutePath.replace(
                                        srcDir.absolutePath + File.separator,
                                        ""
                                    )
                                        .replace(File.separator, ".")
                                val file = File(packageDir, "package-info.kt")
                                val packageInfoFile = PackageInfo(
                                    type = JavaType("$packageinfo.package-info"),
                                    directorySet = DirectorySet.KOTLIN,
                                    overwrite = file.exists() && (file.readLines().size == 1 || file.readText()
                                        .replace(
                                            """/**
 * 
 */
""", ""
                                        ).startsWith("package"))
                                )
                                packageInfoFile.apply {
                                    javadoc {
                                        +"/**"
                                        +" * ${defaultComment(packageinfo)}"
                                        +" */"
                                    }
                                }
                                packageInfoFile.writeTo(subProject.projectDir)
                            }
                        }
                    }
                }
            })
        }

        project.tasks.create("gen[PackageInfoDoc]") { task ->
            task.group = GeneratorPlugin.genGroup
            task.doLast(object : Action<Task> {
                override fun execute(it: Task) {
                    val regex = Regex(".*/\\*\\*(.*)\\*/.*", RegexOption.DOT_MATCHES_ALL)
                    val pregex = Regex("package ([^;]*);?")
                    val dest = FileUnit(
                        name = "doc/项目目录及包目录说明.adoc"
                    )
                    dest.apply {
                        val projects = project.allprojects.filter { p ->
                            p.file("src/main").walkTopDown().filter { it.isFile }.count() > 0
                        }
                        +"= 项目目录及包目录说明"
                        +""":doctype: book
:icons: font
:source-highlighter: highlightjs
:toc: left
:toc-title: 目录
:sectanchors:
:table-caption!:
:sectnums:
:sectlinks:"""
                        +""
                        +"== 项目目录说明"
                        +""
                        +"|==="
                        +"| 项目目录 | 描述"
                        project.projectDir.walkTopDown()
                            .filter { it.isFile && ("README.md" == it.name) }
                            .sortedBy { it.parentFile.absolutePath.substringAfter(project.projectDir.absolutePath + "/") }
                            .forEach {
                                +"| ${it.parentFile.absolutePath.substringAfter(project.projectDir.absolutePath + "/")} | ${
                                    it.readText().trim()
                                }"
                            }
                        +"|==="
                        +""

                        +"== 包目录说明"
                        +""

                        projects.forEach { p ->
                            val files = p.projectDir.walkTopDown()
                                .filter { it.isFile && ("package-info.kt" == it.name || "package-info.java" == it.name) }
                            if (files.any()) {
                                val projectPath = if (p == project.rootProject) {
                                    "主项目"
                                } else {
                                    val pfile = p.file("README.md")
                                    "${
                                        if (pfile.exists()) "${
                                            pfile.readText().trim().substringBefore("\n")
                                        }(${p.path})" else p.path
                                    }子项目"
                                }
                                +"=== ${projectPath}包目录说明"
                                +""
                                +"|==="
                                +"| 包目录 | 描述"
                                files.forEach { file ->
                                    +"| ${
                                        file.readLines().find { it.matches(pregex) }!!
                                            .replace(pregex, "$1")
                                    } | ${
                                        file.readText().replace(regex, "$1").replace("*", "").trim()
                                    }"
                                }
                                +"|==="
                                +""
                            }
                        }
                    }
                    dest.writeTo(project.projectDir)
                }
            })
        }
    }

    private fun defaultComment(packageinfo: String): String {
        val lastPackageinfo = packageinfo.substringAfterLast(".")
        return when {
            "dic" == lastPackageinfo -> "码表"
            packageinfo.endsWith("dic.enumerated") -> "码表枚举"
            packageinfo.endsWith("dic.item") -> "码表常量"
            "controller" == lastPackageinfo -> "Controller控制层"
            "entity" == lastPackageinfo -> "数据实体类"
            "domain" == lastPackageinfo -> "数据实体类"
            "feign" == lastPackageinfo -> "feign RPC请求包"
            "form" == lastPackageinfo -> "请求表单包"
            "info" == lastPackageinfo -> "实体属性信息包"
            "repository" == lastPackageinfo -> "存储库操作层"
            "mixin" == lastPackageinfo -> "JSON序列化映射包"
            "response" == lastPackageinfo -> "数据响应包"
            "service" == lastPackageinfo -> "服务层"
            packageinfo.endsWith("service.impl") -> "服务实现包"
            "impl" == lastPackageinfo -> "实现包"
            "support" == lastPackageinfo -> "工具包"
            "util" == lastPackageinfo -> "工具包"
            "utils" == lastPackageinfo -> "工具包"
            "web" == lastPackageinfo -> "WEB 配置包"
            "modules" == lastPackageinfo -> "功能模块"
            else -> ""
        }
    }
}