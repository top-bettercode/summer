package top.bettercode.summer.test.autodoc.field

import top.bettercode.summer.tools.autodoc.model.Field
import top.bettercode.summer.tools.generator.dsl.GenDicCodes
import top.bettercode.summer.tools.lang.property.PropertiesSource

/**
 *
 * @author Peter Wu
 */
class DicCodeFix : FieldDescFix() {

    override val cover: Boolean = true

    private val dicCodeFields: Iterable<Iterable<Field>> by lazy {
        val propertiesSource = PropertiesSource("dic-code", "app-dic-code")

        @Suppress("UNCHECKED_CAST")
        val dicCodes = GenDicCodes.convert(propertiesSource.all() as Map<Any, Any>)
        setOf(dicCodes.map { (_, dicCode) ->
            Field(
                name = dicCode.type, type = dicCode.javaType.shortName, description = "${
                    dicCode.name
                }(${dicCode.codes.entries.joinToString { "${it.key}:${it.value}" }})"
            )
        })
    }

    override fun descFields(properties: DocProperties): Iterable<Iterable<Field>> {
        return dicCodeFields
    }
}