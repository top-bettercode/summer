package top.bettercode.summer.gradle.plugin.project

import org.gradle.api.Project
import top.bettercode.summer.gradle.plugin.autodoc.AsciidocGenerator
import top.bettercode.summer.tools.generator.GeneratorExtension.Companion.defaultModuleName
import top.bettercode.summer.tools.generator.dom.unit.FileUnit
import top.bettercode.summer.tools.generator.dsl.Generator
import java.io.File

/**
 * @author Peter Wu
 */

class DbDoc(private val project: Project) : Generator() {

    private var currentModuleName: String? = null

    override val projectDir: File
        get() = project.rootDir

    private val name get() = "database/doc/${ext.applicationName}数据库设计说明书-${project.version}.adoc"

    override fun setUp() {
        add(file(name)).apply {
            +"= ${ext.applicationName}数据库设计说明书"
            +"""JAVA小组
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
"""
            +"""
系统使用MYSQL5.7数据库
"""
        }
    }

    override fun call() {
        (this[name] as FileUnit).apply {
            +""
            if (table.subModuleName != currentModuleName) {
                +"== ${table.subModuleName}模块"
                +""
                +"模型图如下:"
                +""
                +"[plantuml]"
                +"----"
                +project.rootProject.file("puml/${if (defaultModuleName == table.module) "src" else table.module}/${table.subModule}.puml")
                    .readText()
                +"----"
                currentModuleName = table.subModuleName
            }
            +""
            val tableName = tableName
            +"=== $tableName${if (remarks.isNotBlank()) " ($remarks)" else ""}表"
            +"|==="
            +"|名称|类型|描述|备注"
            +""
            columns.forEach {
                +"| ${it.columnName} | ${it.typeDesc} | ${it.remark} | ${if (it.isPrimary) " PK" else if (it.unique) " UNIQUE" else if (it.indexed) " INDEX" else ""}${it.defaultDesc}${if (it.extra.isNotBlank()) " ${it.extra}" else ""}${if (it.nullable) "" else " NOT NULL"}"
            }
            +"|==="
            +""
        }
    }

    override fun tearDown() {
        val fileUnit = this[name] as FileUnit
        val outFile =
            File(projectDir, "${fileUnit.file.parent}/${fileUnit.file.nameWithoutExtension}.pdf")
        if (setting("dbdoc-pdf") == "true")
            AsciidocGenerator.pdf(fileUnit.outputFile(projectDir), outFile, projectDir)
    }
}


