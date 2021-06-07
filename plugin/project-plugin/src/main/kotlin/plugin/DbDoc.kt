package plugin

import top.bettercode.autodoc.core.AsciidocGenerator
import top.bettercode.generator.dsl.Generator
import org.gradle.api.Project
import java.io.File

/**
 * @author Peter Wu
 */

class DbDoc(private val project: Project) : Generator() {

    private var currentModuleName: String? = null
    private lateinit var file: File

    override fun setUp() {
        this.file = project.rootProject.file("database/doc/${extension.applicationName}数据库设计说明书-${project.version}.adoc")
        this.file.parentFile.mkdirs()
        this.file.writeText("= ${extension.applicationName}数据库设计说明书\n")
        this.file.appendText("""JAVA小组
v${project.version}
:doctype: book
:icons: font
:source-highlighter: highlightjs
:toc: left
:toc-title: 目录
:sectanchors:
:table-caption!:
:sectlinks:
:toclevels: 2
:sectnums:
""")
        this.file.appendText("""
系统使用MYSQL5.7数据库
""")
    }

    override fun doCall() {
        file.appendText("\n")
        if (moduleName != currentModuleName) {
            file.appendText("== ${moduleName}模块\n")
            file.appendText("\n")
            file.appendText("模型图如下:\n")
            file.appendText("\n")
            file.appendText("[plantuml]\n----\n")
            file.appendText("${project.rootProject.file("puml/src/$module.puml").readText()}\n")
            file.appendText("----\n")
            currentModuleName = moduleName
        }
        file.appendText("\n")
        val tableName = tableName
        file.appendText("=== $tableName${if (remarks.isNotBlank()) " ($remarks)" else ""}表\n")
        file.appendText("|===\n")
        file.appendText("|名称|类型|描述|备注")
        file.appendText("\n")
        columns.forEach {
            file.appendText("| ${it.columnName} | ${it.typeDesc} | ${it.remarks} | ${if (it.isPrimary) " PK" else if (it.unique) " UNIQUE" else if (it.indexed) " INDEX" else ""}${it.defaultDesc}${if (it.extra.isNotBlank()) " ${it.extra}" else ""}${if (it.nullable) "" else " NOT NULL"}\n")
        }
        file.appendText("|===\n")
        file.appendText("\n")
    }

    override fun tearDown() {
        top.bettercode.autodoc.core.AsciidocGenerator.pdf(file, File(file.parent, "${file.nameWithoutExtension}.pdf"))
    }
}


