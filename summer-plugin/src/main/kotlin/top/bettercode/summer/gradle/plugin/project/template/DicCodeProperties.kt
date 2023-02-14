package top.bettercode.summer.gradle.plugin.project.template

import top.bettercode.summer.tools.generator.dom.unit.PropertiesUnit
import top.bettercode.summer.tools.generator.dsl.Generator

/**
 * @author Peter Wu
 */
class DicCodeProperties : Generator() {
    private val codeTypes: MutableMap<String, Int> = mutableMapOf()
    private val name = "default-dic-code.properties"
    override fun setUp() {
        add(properties(name, overwrite = true))
    }

    override fun call() {
        (this[name] as PropertiesUnit).apply {
            columns.forEach { col ->
                if (col.isCodeField) {
                    val dicCodes = col.dicCodes(ext)!!
                    val codeType = dicCodes.type
                    val size = dicCodes.codes.size
                    if (!codeTypes.contains(codeType) || (codeTypes[codeType] ?: 0) > size) {
                        codeTypes[codeType] = size
                        this[codeType] = dicCodes.name
                        this["$codeType|TYPE"] = if (dicCodes.isInt) "Int" else "String"
                        dicCodes.codes.forEach {
                            this["$codeType.${it.key}"] = it.value
                        }
                    }
                }
            }
        }
    }

}


