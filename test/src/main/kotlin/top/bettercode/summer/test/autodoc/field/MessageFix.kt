package top.bettercode.summer.test.autodoc.field

import top.bettercode.summer.tools.autodoc.model.Field
import top.bettercode.summer.tools.lang.property.PropertiesSource

/**
 *
 * @author Peter Wu
 */
class MessageFix : FieldDescFix() {

    override val cover: Boolean = true

    private val messageFields: Set<Field> by lazy {
        setOf(Field(name = "lines", description = "行信息")) +
                PropertiesSource.of("messages").all()
                    .map { Field(name = it.key, description = it.value) }
                    .toSet()
    }

    override fun descFields(properties: DocProperties): Set<Field> {
        return messageFields
    }

}