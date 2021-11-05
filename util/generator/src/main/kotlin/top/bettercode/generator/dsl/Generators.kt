package top.bettercode.generator.dsl

import top.bettercode.generator.DataType
import top.bettercode.generator.GeneratorExtension
import top.bettercode.generator.dom.java.JavaTypeResolver
import top.bettercode.generator.powerdesigner.PdmReader
import top.bettercode.generator.puml.PumlConverter

/**
 * 模板脚本
 * @author Peter Wu

 */
object Generators {

    /**
     * @param extension 配置
     */
    fun call(extension: GeneratorExtension) {
        JavaTypeResolver.softDeleteColumnName = extension.softDeleteColumnName
        JavaTypeResolver.softDeleteAsBoolean = extension.softDeleteAsBoolean
        JavaTypeResolver.useJSR310Types = extension.useJSR310Types

        if (extension.generators.isEmpty()) {
            return
        }
        when (extension.dataType) {
            DataType.DATABASE -> database(extension)
            DataType.PUML -> puml(extension)
            DataType.PDM -> pdm(extension)
        }
    }

    fun tableNames(extension: GeneratorExtension): List<String> {
        JavaTypeResolver.softDeleteColumnName = extension.softDeleteColumnName
        JavaTypeResolver.softDeleteAsBoolean = extension.softDeleteAsBoolean
        return when (extension.dataType) {
            DataType.DATABASE -> extension.use {
                tableNames()
            }
            DataType.PUML -> {
                extension.pumlSrcSources.map { PumlConverter.toTables(it) }.flatten()
                    .map { it.tableName }.distinct().sortedBy { it }.toList()
            }
            DataType.PDM -> {
                PdmReader.read(extension.file(extension.pdmSrc)).map { it.tableName }.distinct().sortedBy { it }
                    .toList()
            }
        }
    }

    fun pdm(extension: GeneratorExtension) {
        val generators = extension.generators
        if (generators.isEmpty()) {
            return
        }
        generators.forEach { generator ->
            generator.setUp(extension)
        }

        val tables = PdmReader.read(extension.file(extension.pdmSrc))
        if (extension.tableNames.isEmpty()) {
            extension.tableNames = tables.map { it.tableName }.toTypedArray()
        }

        extension.tableNames.forEach { tableName ->
            val table = tables.find { it.tableName == tableName }
                ?: throw RuntimeException("未在(${extension.tableNames})中找到${tableName}表")
            generators.forEach { generator ->
                generator.call(extension, table)
            }
        }
        generators.forEach { generator ->
            generator.tearDown(extension)
        }
    }

    fun database(extension: GeneratorExtension) {
        val generators = extension.generators
        if (generators.isEmpty()) {
            return
        }
        generators.forEach { generator ->
            generator.setUp(extension)
        }

        extension.use {
            if (extension.tableNames.isEmpty()) {
                extension.use {
                    extension.tableNames = tableNames().toTypedArray()
                }
            }
            extension.tableNames.forEach {
                val table = table(it)
                if (table != null) {
                    generators.forEach { generator ->
                        generator.call(extension, table)
                    }
                }
            }
        }
        generators.forEach { generator ->
            generator.tearDown(extension)
        }
    }

    fun puml(extension: GeneratorExtension) {
        val generators = extension.generators
        if (generators.isEmpty()) {
            return
        }
        generators.forEach { generator ->
            generator.setUp(extension)
        }

        var tableNames = extension.tableNames
        if (tableNames.isEmpty())
            tableNames = tableNames(extension).toTypedArray()


        tableNames.forEach inner@{ tableName ->
            var found = false
            val allTableNames = mutableSetOf<String>()
            extension.pumlAllSources.forEach { file ->
                val tables = PumlConverter.toTables(file)
                val table = tables.find { it.tableName == tableName }
                if (table != null) {
                    found = true
                    generators.forEach { generator ->
                        generator.module = file.nameWithoutExtension
                        generator.call(extension, table)
                    }
                    return@inner
                } else {
                    allTableNames.addAll(tables.map { it.tableName })
                }
            }
            if (!found)
                throw RuntimeException("未在($allTableNames)中找到${tableName}表")
        }

        generators.forEach { generator ->
            generator.tearDown(extension)
        }
    }

}