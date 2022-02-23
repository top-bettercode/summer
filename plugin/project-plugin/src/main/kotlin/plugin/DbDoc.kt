package plugin

import org.gradle.api.Project
import top.bettercode.autodoc.core.AsciidocGenerator
import top.bettercode.generator.defaultModuleName
import top.bettercode.generator.dom.java.element.FileUnit
import top.bettercode.generator.dsl.Generator
import java.io.File

/**
 * @author Peter Wu
 */

class DbDoc(private val project: Project) : Generator() {

    private var currentModuleName: String? = null
    private val file: FileUnit by lazy {
        FileUnit(
            "database/doc/${extension.applicationName}数据库设计说明书-${project.version}.adoc",
            canCover = true,
            isRootFile = true
        )
    }

    override fun setUp() {
        file.apply {
            +"= ${extension.applicationName}数据库设计说明书"
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
        file.apply {
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
                +"| ${it.columnName} | ${it.typeDesc} | ${it.remarks} | ${if (it.isPrimary) " PK" else if (it.unique) " UNIQUE" else if (it.indexed) " INDEX" else ""}${it.defaultDesc}${if (it.extra.isNotBlank()) " ${it.extra}" else ""}${if (it.nullable) "" else " NOT NULL"}"
            }
            +"|==="
            +""
        }
    }

    override fun tearDown() {
        file.write()
        val outFile = File(file.file.parent, "${file.file.nameWithoutExtension}.pdf")
        if (setting("dbdoc-pdf") == "true")
            AsciidocGenerator.pdf(file.file, outFile, project.rootDir)
    }
}


