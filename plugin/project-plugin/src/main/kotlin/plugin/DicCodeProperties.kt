package plugin

import top.bettercode.generator.SortedProperties
import top.bettercode.generator.dom.java.element.ManualUnit
import top.bettercode.generator.dsl.Generator
import java.io.File

/**
 * @author Peter Wu
 */
class DicCodeProperties : Generator() {
    private val codeTypes: MutableMap<String, Int> = mutableMapOf()
    private lateinit var properties: SortedProperties
    private lateinit var unit: ManualUnit

    override fun setUp() {
        properties = SortedProperties()
        unit = ManualUnit("default-dic-code.properties", isResourcesFile = true)
        if (!unit.file.exists()) {
            unit.file.createNewFile()
        }
    }

    override fun tearDown() {
        properties.store(unit.file.outputStream(), "国际化")
        println(
            "${if (unit.file.exists()) "覆盖" else "生成"}：${
                unit.file.absolutePath.substringAfter(
                    (extension.rootPath ?: extension.basePath).absolutePath + File.separator
                )
            }"
        )
    }

    override fun call() {
        columns.forEach { col ->
            if (col.isCodeField) {
                val dicCodes = col.dicCodes(extension)!!
                val codeType = dicCodes.type
                val size = dicCodes.codes.size
                if (!codeTypes.contains(codeType) || (codeTypes[codeType] ?: 0) > size) {
                    codeTypes[codeType] = size
                    properties[codeType] = dicCodes.name
                    properties["$codeType|TYPE"] = if (dicCodes.isInt) "Int" else "String"
                    dicCodes.codes.forEach {
                        properties["$codeType.${it.key}"] = it.value
                    }
                }
            }
        }
    }

}


