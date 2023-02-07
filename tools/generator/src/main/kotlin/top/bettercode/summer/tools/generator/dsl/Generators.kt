package top.bettercode.summer.tools.generator.dsl

import top.bettercode.summer.tools.generator.GeneratorExtension
import top.bettercode.summer.tools.generator.TableHolder

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

    fun tableNamesWithOutPrefix(extension: GeneratorExtension): List<String> {
        return extension.run { module, tableHolder ->
            tableHolder.tableNames().map { tableName ->
                (extension.datasource(module)).fixTableName(
                    tableName
                )
            }
        }.flatten().sortedBy { it }
    }


    /**
     * @param extension 配置
     */
    fun call(extension: GeneratorExtension) {
        extension.run { _, tableHolder ->
            call(extension, tableHolder)
        }
    }

    fun call(extension: GeneratorExtension, tableHolder: TableHolder) {
        val generators = extension.generators
        if (generators.isEmpty()) {
            return
        }
        generators.forEach { generator ->
            generator.setUp(extension)
        }

        tableHolder.tables(
            checkFound = when (extension.dataType) {
                top.bettercode.summer.tools.generator.DataType.DATABASE -> {
                    extension.datasources.size <= 1
                }

                top.bettercode.summer.tools.generator.DataType.PUML -> {
                    extension.pumlSources.size <= 1
                }

                top.bettercode.summer.tools.generator.DataType.PDM -> {
                    extension.pdmSources.size <= 1
                }
            }, tableName = extension.tableNames
        ).forEach { table ->
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