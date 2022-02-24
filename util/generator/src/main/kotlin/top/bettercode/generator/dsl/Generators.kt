package top.bettercode.generator.dsl

import top.bettercode.generator.DataType
import top.bettercode.generator.GeneratorExtension
import top.bettercode.generator.database.entity.Table
import top.bettercode.generator.dom.java.JavaTypeResolver
import top.bettercode.generator.powerdesigner.PdmReader
import top.bettercode.generator.puml.PumlConverter

/**
 * 模板脚本
 * @author Peter Wu

 */
object Generators {

    fun tableNames(extension: GeneratorExtension): List<String> {
        JavaTypeResolver.softDeleteColumnName = extension.softDeleteColumnName
        JavaTypeResolver.softDeleteAsBoolean = extension.softDeleteAsBoolean
        return when (extension.dataType) {
            DataType.DATABASE -> {
                extension.datasources.map { (_, jdbc) ->
                    jdbc.tableNames()
                }
            }
            DataType.PUML -> {
                extension.pumlSources.map { (module, files) ->
                    files.map { file ->
                        PumlConverter.toTables(
                            file,
                            module
                        ).map { it.tableName }
                    }.flatten()
                }
            }
            DataType.PDM -> {
                extension.pdmSources.map { (module, files) ->
                    files.map { file ->
                        PdmReader.read(
                            file,
                            module
                        ).map { it.tableName }
                    }
                }.flatten()
            }
        }.flatten().sortedBy { it }
    }

    /**
     * @param extension 配置
     */
    fun call(extension: GeneratorExtension) {
        JavaTypeResolver.softDeleteColumnName = extension.softDeleteColumnName
        JavaTypeResolver.softDeleteAsBoolean = extension.softDeleteAsBoolean
        JavaTypeResolver.useJSR310Types = extension.useJSR310Types

        val generators = extension.generators
        if (generators.isEmpty()) {
            return
        }
        generators.forEach { generator ->
            generator.setUp(extension)
        }
        val all = extension.tableNames.isEmpty()
        val tableNames = extension.tableNames.toMutableList()
        val toTables = { toTables: () -> List<Table> ->
            if (all) {
                toTables()
            } else {
                if (tableNames.isNotEmpty()) {
                    val tables = toTables()
                    tableNames.removeAll(tables.map { it.tableName })
                    tables
                } else
                    emptyList()
            }
        }
        val tables = when (extension.dataType) {
            DataType.DATABASE -> {
                extension.datasources.mapValues { (_, jdbc) ->
                    toTables {
                        if (all) {
                            jdbc.tables(jdbc.tableNames())
                        } else {
                            jdbc.tables(tableNames)
                        }
                    }
                }.values
            }
            DataType.PUML -> {
                extension.pumlSources.map { (module, files) ->
                    files.map { file ->
                        toTables {
                            val tables = PumlConverter.toTables(file, module)
                            if (all) {
                                tables
                            } else {
                                tables.filter { tableNames.contains(it.tableName) }
                            }
                        }
                    }
                }.flatten()
            }
            DataType.PDM -> {
                extension.pdmSources.map { (module, files) ->
                    files.map { file ->
                        toTables {
                            val tables = PdmReader.read(file, module)
                            if (all) {
                                tables
                            } else {
                                tables.filter { tableNames.contains(it.tableName) }
                            }
                        }
                    }
                }.flatten()
            }
        }.flatten()
        tables.sortedBy { it.tableName }.forEach { table ->
            generators.forEach { generator ->
                generator.run(table)
            }
        }
        generators.forEach { generator ->
            generator.preTearDown()
            generator.tearDown()
        }
    }
}