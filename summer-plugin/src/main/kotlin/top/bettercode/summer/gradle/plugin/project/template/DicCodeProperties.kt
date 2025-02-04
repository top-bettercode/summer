package top.bettercode.summer.gradle.plugin.project.template

import top.bettercode.summer.tools.generator.dom.unit.PropertiesUnit
import top.bettercode.summer.tools.generator.dsl.Generator

/**
 * @author Peter Wu
 */
class DicCodeProperties : Generator() {
    private val name = "default-dic-code.properties"
    override fun setUp() {
        add(properties(name, overwrite = true))
    }

    override fun call() {
        (this[name] as PropertiesUnit).apply {
            columns.forEach { col ->
                if (col.isCodeField) {
                    val dicCodes = col.dicCodes()!!
                    val codeType = dicCodes.type

                    if (!this.contains(codeType)) {
                        this[codeType] = dicCodes.name
                        this["$codeType|TYPE"] =
                            dicCodes.javaType.fullyQualifiedNameWithoutTypeParameters
                        dicCodes.codes.forEach {
                            this["$codeType.${it.key}"] = it.value
                        }
                    }
                }
            }
        }
    }

}


