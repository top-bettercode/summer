package top.bettercode.generator.dsl

import top.bettercode.generator.DataType
import top.bettercode.generator.GeneratorExtension

/**
 * 模板脚本
 * @author Peter Wu

 */
object Generators {

    fun tableNames(extension: GeneratorExtension): List<String> {
        return extension.run { _, tableHolder ->
            tableHolder.tableNames()
        }.flatten().sortedBy { it }
    }

    /**
     * @param extension 配置
     */
    fun call(extension: GeneratorExtension) {
        val generators = extension.generators
        if (generators.isEmpty()) {
            return
        }
        generators.forEach { generator ->
            generator.setUp(extension)
        }

        extension.run { _, tableHolder ->
            tableHolder.tables(
                checkFound = when (extension.dataType) {
                    DataType.DATABASE -> {
                        extension.datasources.size <= 1
                    }
                    DataType.PUML -> {
                        extension.pumlSources.size <= 1
                    }
                    DataType.PDM -> {
                        extension.pdmSources.size <= 1
                    }
                }, tableName = extension.tableNames
            ).forEach { table ->
                generators.forEach { generator ->
                    generator.run(table)
                }
            }
        }

        generators.forEach { generator ->
            generator.preTearDown()
            generator.tearDown()
        }
    }
}