package cn.bestwu.gradle.generator

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.plugins.JavaPluginConvention
import org.gradle.api.tasks.SourceSet
import java.io.File

/**
 *
 * @author Peter Wu
 */

class GenPackageinfoPlugin : Plugin<Project> {

    override fun apply(project: Project) {

        project.tasks.create("packageInfo") { task ->
            task.group = "gen"
            task.doLast { _ ->
                project.allprojects { p ->
                    p.convention.getPlugin(JavaPluginConvention::class.java).sourceSets.getByName(SourceSet.MAIN_SOURCE_SET_NAME).java.srcDirs.forEach { file ->
                        val srcPath = file.absolutePath + File.separator
                        file.walkTopDown().filter { it.isDirectory }.forEach { file1 ->
                            val packageInfoFile = File(file1, "package-info.java")
                            if (packageInfoFile.exists() && (packageInfoFile.readLines().size == 1 || packageInfoFile.readText().replace("""/**
 * 
 */
""", "").startsWith("package"))) {
                                packageInfoFile.delete()
                            }
                            val listFiles = file1.listFiles()
                            if (!packageInfoFile.exists() && listFiles != null && (listFiles.count() > 1 || listFiles.any { it.isFile })) {
                                println("[${p.path}]生成：${packageInfoFile.absolutePath.substringAfter(p.file("./").absolutePath)}")
                                val packageinfo = file1.absolutePath.replace(srcPath, "").replace(File.separator, ".")
                                packageInfoFile.writeText("""/**
 * ${defaultComment(packageinfo)}
 */
package ${packageinfo};""")
                            }
                        }
                    }
                    val file = p.file("src/main/kotlin")
                    val srcPath = file.absolutePath + File.separator
                    file.walkTopDown().filter { it.isDirectory }.forEach { file1 ->
                        val packageInfoFile = File(file1, "package-info.kt")
                        if (packageInfoFile.exists() && (packageInfoFile.readLines().size == 1 || packageInfoFile.readText().replace("""/**
 * 
 */
""", "").startsWith("package"))) {
                            packageInfoFile.delete()
                        }
                        val listFiles = file1.listFiles()
                        if (!packageInfoFile.exists() && listFiles != null && (listFiles.count() > 1 || listFiles.any { it.isFile })) {
                            println("[${p.path}]生成：${packageInfoFile.absolutePath.substringAfter(p.file("./").absolutePath)}")
                            val packageinfo = file1.absolutePath.replace(srcPath, "").replace(File.separator, ".")
                            packageInfoFile.writeText("""/**
 * ${defaultComment(packageinfo)}
 */
package $packageinfo""".trimIndent())
                        }
                    }
                }
            }
        }

        project.tasks.create("packageInfoDoc") { task ->
            task.group = "gen"
            task.doLast { _ ->
                val regex = Regex(".*/\\*\\*(.*)\\*/.*", RegexOption.DOT_MATCHES_ALL)
                val pregex = Regex("package ([^;]*);?")
                val dest = project.file("doc/项目目录及包目录说明.adoc")
                if (!dest.parentFile.exists()) {
                    dest.parentFile.mkdirs()
                }
                dest.printWriter().use { pw ->
                    val projects = project.allprojects.filter { p -> p.file("src/main").walkTopDown().filter { it.isFile }.count() > 0 }
                    pw.println("= 项目目录及包目录说明")
                    pw.println(""":doctype: book
:icons: font
:source-highlighter: highlightjs
:toc: left
:toc-title: 目录
:sectanchors:
:table-caption!:
:sectnums:
:sectlinks:""")
                    pw.println()
                    pw.println("== 项目目录说明")
                    pw.println()
                    pw.println("|===")
                    pw.println("| 项目目录 | 描述")
                    project.file("./").walkTopDown().filter { it.isFile && ("README.md" == it.name) }.sortedBy { it.parentFile.absolutePath.substringAfter(project.file("./").absolutePath + "/") }.forEach {
                        pw.println("| ${it.parentFile.absolutePath.substringAfter(project.file("./").absolutePath + "/")} | ${it.readText().trim()}")
                    }
                    pw.println("|===")
                    pw.println()

                    pw.println("== 包目录说明")
                    pw.println()

                    projects.forEach { p ->
                        val files = p.file("./").walkTopDown().filter { it.isFile && ("package-info.kt" == it.name || "package-info.java" == it.name) }
                        if (files.any()) {
                            val projectPath = if (p == project.rootProject) {
                                "主项目"
                            } else {
                                val pfile = p.file("README.md")
                                "${if (pfile.exists()) "${pfile.readText().trim().substringBefore("\n")}(${p.path})" else p.path}子项目"
                            }
                            pw.println("=== ${projectPath}包目录说明")
                            pw.println()
                            pw.println("|===")
                            pw.println("| 包目录 | 描述")
                            files.forEach { file ->
                                pw.println("| ${file.readLines().find { it.matches(pregex) }!!.replace(pregex, "$1")} | ${file.readText().replace(regex, "$1").replace("*", "").trim()}")
                            }
                            pw.println("|===")
                            pw.println()
                        }
                    }
                }
            }
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
            else -> ""
        }
    }
}