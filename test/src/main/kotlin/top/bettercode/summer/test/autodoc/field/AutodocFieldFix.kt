package top.bettercode.summer.test.autodoc.field

import top.bettercode.summer.test.autodoc.Autodoc
import top.bettercode.summer.tools.autodoc.model.Field

/**
 *
 * @author Peter Wu
 */
class AutodocFieldFix : FieldDescFix() {

    override val cover: Boolean = true

    private val fields: Set<Field> by lazy {
        Autodoc.fields.toSet()
    }

    override fun descFields(properties: DocProperties): Set<Field> {
        return fields
    }
}