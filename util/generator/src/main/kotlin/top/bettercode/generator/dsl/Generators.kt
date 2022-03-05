package top.bettercode.generator.dsl

import top.bettercode.generator.GeneratorExtension
import top.bettercode.generator.dom.java.JavaTypeResolver

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

        extension.run { _, tableHolder ->
            tableHolder.tables(*extension.tableNames).forEach { table ->
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