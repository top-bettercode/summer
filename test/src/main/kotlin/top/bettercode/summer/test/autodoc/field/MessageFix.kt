package top.bettercode.summer.test.autodoc.field

import top.bettercode.summer.tools.autodoc.model.Field
import top.bettercode.summer.tools.lang.property.PropertiesSource

/**
 *
 * @author Peter Wu
 */
class MessageFix : FieldDescFix() {

    override val cover: Boolean = true

    private val messageFields: Iterable<Iterable<Field>> by lazy {
        val fields = PropertiesSource.of("messages").all()
            .map { Field(name = it.key, description = it.value) }
        setOf(fields + Field(name = "lines", description = "行信息"))
    }

    override fun descFields(properties: DocProperties): Iterable<Iterable<Field>> {
        return messageFields
    }

}