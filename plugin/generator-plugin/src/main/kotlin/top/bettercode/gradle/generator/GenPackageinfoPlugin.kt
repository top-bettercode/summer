package top.bettercode.gradle.generator

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.plugins.JavaPluginExtension
import org.gradle.api.tasks.SourceSet
import top.bettercode.generator.dom.java.JavaType
import top.bettercode.generator.dom.java.element.PackageInfo
import top.bettercode.generator.dom.unit.DirectorySet
import top.bettercode.generator.dom.unit.FileUnit
import java.io.File

/**
 *
 * @author Peter Wu
 */

class GenPackageinfoPlugin : Plugin<Project> {

    override fun apply(project: Project) {

        project.tasks.create("gen[PackageInfo]") { task ->
            task.group = GeneratorPlugin.genGroup
            task.doLast { _ ->
                project.allprojects { subProject ->
                    subProject.extensions.getByType(JavaPluginExtension::class.java).sourceSets.getByName(
                        SourceSet.MAIN_SOURCE_SET_NAME
                    ).java.srcDirs.forEach { srcDir ->
                        srcDir.walkTopDown().filter { it.isDirectory }.forEach { packageDir ->
                            val listFiles =
                                packageDir.listFiles()?.filter { it.name != "package-info.java" }
                            if (listFiles != null && (listFiles.count() > 1 || listFiles.any { it.isFile })) {
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
                        if (listFiles != null && (listFiles.count() > 1 || listFiles.any { it.isFile })) {
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
        }

        project.tasks.create("gen[PackageInfoDoc]") { task ->
            task.group = GeneratorPlugin.genGroup
            task.doLast { _ ->
                val regex = Regex(".*/\\*\\*(.*)\\*/.*", RegexOption.DOT_MATCHES_ALL)
                val pregex = Regex("package ([^;]*);?")
                val dest = FileUnit(
                    name = "doc/??????????????????????????????.adoc"
                )
                dest.apply {
                    val projects = project.allprojects.filter { p ->
                        p.file("src/main").walkTopDown().filter { it.isFile }.count() > 0
                    }
                    +"= ??????????????????????????????"
                    +""":doctype: book
:icons: font
:source-highlighter: highlightjs
:toc: left
:toc-title: ??????
:sectanchors:
:table-caption!:
:sectnums:
:sectlinks:"""
                    +""
                    +"== ??????????????????"
                    +""
                    +"|==="
                    +"| ???????????? | ??????"
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

                    +"== ???????????????"
                    +""

                    projects.forEach { p ->
                        val files = p.projectDir.walkTopDown()
                            .filter { it.isFile && ("package-info.kt" == it.name || "package-info.java" == it.name) }
                        if (files.any()) {
                            val projectPath = if (p == project.rootProject) {
                                "?????????"
                            } else {
                                val pfile = p.file("README.md")
                                "${
                                    if (pfile.exists()) "${
                                        pfile.readText().trim().substringBefore("\n")
                                    }(${p.path})" else p.path
                                }?????????"
                            }
                            +"=== ${projectPath}???????????????"
                            +""
                            +"|==="
                            +"| ????????? | ??????"
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
        }
    }

    private fun defaultComment(packageinfo: String): String {
        val lastPackageinfo = packageinfo.substringAfterLast(".")
        return when {
            "dic" == lastPackageinfo -> "??????"
            packageinfo.endsWith("dic.enumerated") -> "????????????"
            packageinfo.endsWith("dic.item") -> "????????????"
            "controller" == lastPackageinfo -> "Controller?????????"
            "entity" == lastPackageinfo -> "???????????????"
            "domain" == lastPackageinfo -> "???????????????"
            "feign" == lastPackageinfo -> "feign RPC?????????"
            "form" == lastPackageinfo -> "???????????????"
            "info" == lastPackageinfo -> "?????????????????????"
            "repository" == lastPackageinfo -> "??????????????????"
            "mixin" == lastPackageinfo -> "JSON??????????????????"
            "response" == lastPackageinfo -> "???????????????"
            "service" == lastPackageinfo -> "?????????"
            packageinfo.endsWith("service.impl") -> "???????????????"
            "impl" == lastPackageinfo -> "?????????"
            "support" == lastPackageinfo -> "?????????"
            "util" == lastPackageinfo -> "?????????"
            "utils" == lastPackageinfo -> "?????????"
            "web" == lastPackageinfo -> "WEB ?????????"
            "modules" == lastPackageinfo -> "????????????"
            else -> ""
        }
    }
}