package cn.bestwu.gradle.generator

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.plugins.JavaPluginConvention
import org.gradle.api.tasks.SourceSet
import java.io.File
import java.util.regex.Pattern

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
                            val packageInfo = File(file1, "package-info.java")
                            val listFiles = file1.listFiles()
                            if (!packageInfo.exists() && listFiles != null && (listFiles.count() > 1 || listFiles.any { it.isFile })) {
                                println("[${p.path}]生成：${packageInfo.absolutePath.substringAfter(p.file("./").absolutePath)}")
                                packageInfo.writeText("""package ${file1.absolutePath.replace(srcPath, "").replace(File.separator, ".")};""")
                            }
                        }
                    }
                    val file = p.file("src/main/kotlin")
                    val srcPath = file.absolutePath + File.separator
                    file.walkTopDown().filter { it.isDirectory }.forEach { file1 ->
                        val packageInfo = File(file1, "package-info.kt")
                        val listFiles = file1.listFiles()
                        if (!packageInfo.exists() && listFiles != null && (listFiles.count() > 1 || listFiles.any { it.isFile })) {
                            println("[${p.path}]生成：${packageInfo.absolutePath.substringAfter(p.file("./").absolutePath)}")
                            packageInfo.writeText("""package ${file1.absolutePath.replace(srcPath, "").replace(File.separator, ".")}""")
                        }
                    }
                }
            }
        }

        project.tasks.create("packageInfoDoc") { task ->
            task.group = "gen"
            task.doLast { _ ->
                val regex = Regex(".*/\\*\\*(.*)\\*/.*", RegexOption.DOT_MATCHES_ALL)
                val pregex = Regex("package (.*);?")
                val dest = project.file("doc/项目目录及包目录说明.adoc")
                if (!dest.parentFile.exists()) {
                    dest.parentFile.mkdirs()
                }
                dest.printWriter().use { pw ->
                    pw.println("= 项目目录及包目录说明")
                    pw.println()
                    pw.println("== 项目目录说明")
                    pw.println()
                    pw.println("|===")
                    pw.println("| 项目目录 | 描述")
                    project.file("./").walkTopDown().filter { it.isFile && ("README.md" == it.name) }.forEach {
                        pw.println("| ${it.parentFile.absolutePath.substringAfter(project.file("./").absolutePath + "/")} | ${it.readText().trim()}")
                    }
                    pw.println("|===")

                    pw.println("== 包目录说明")
                    pw.println()
                    project.allprojects { p ->
                        val files = p.file("./").walkTopDown().filter { it.isFile && ("package-info.kt" == it.name || "package-info.java" == it.name) }
                        if (files.any()) {
                            val projectPath = if (p == project.rootProject) {
                                "主项目"
                            } else {
                                val pfile = p.file("README.md")
                                "${if (pfile.exists()) pfile.readText().trim().substringBefore("\n") else p.path}子项目"
                            }
                            pw.println("=== ${projectPath}包目录说明")
                            pw.println()
                            pw.println("|===")
                            pw.println("| 包目录 | 描述")
                            files.forEach { file ->
                                val text = file.readText()
                                pw.println("| ${file.readLines().find { it.matches(pregex) }!!.replace(pregex, "$1")} | ${text.replace(regex, "$1").trim().trimStart('*').trim()}")
                            }
                            pw.println("|===")
                        }
                    }
                }
            }
        }
    }
}