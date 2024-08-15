package top.bettercode.summer.test.autodoc.field

import top.bettercode.summer.test.autodoc.Autodoc
import top.bettercode.summer.tools.autodoc.model.Field

/**
 *
 * @author Peter Wu
 */
class AutodocFieldFix : FieldDescFix() {

    override val cover: Boolean = true

    private val fields: Iterable<Iterable<Field>> by lazy {
        Autodoc.fields.values
    }

    override fun descFields(properties: DocProperties): Iterable<Iterable<Field>> {
        return fields
    }
}