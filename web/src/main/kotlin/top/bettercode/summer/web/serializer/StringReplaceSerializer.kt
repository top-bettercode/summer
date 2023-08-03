package top.bettercode.summer.web.serializer

import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.databind.BeanProperty
import com.fasterxml.jackson.databind.JsonSerializer
import com.fasterxml.jackson.databind.SerializerProvider
import com.fasterxml.jackson.databind.annotation.JacksonStdImpl
import com.fasterxml.jackson.databind.jsontype.TypeSerializer
import com.fasterxml.jackson.databind.ser.ContextualSerializer
import com.fasterxml.jackson.databind.ser.std.StdScalarSerializer
import org.springframework.util.StringUtils
import top.bettercode.summer.web.serializer.annotation.JsonStringReplace

/**
 * @author Peter Wu
 */
@JacksonStdImpl
class StringReplaceSerializer : StdScalarSerializer<String>, ContextualSerializer {
    private val target: String
    private val replacement: String

    constructor() : super(String::class.java, false) {
        target = ""
        replacement = ""
    }

    constructor(target: String, replacement: String) : super(String::class.java, false) {
        this.target = target
        this.replacement = replacement
    }

    override fun serialize(value: String, gen: JsonGenerator, provider: SerializerProvider) {
        gen.writeString(if (StringUtils.hasText(value) && StringUtils.hasText(target)) value
                .replace(target, replacement) else value)
    }

    override fun createContextual(prov: SerializerProvider, property: BeanProperty?): JsonSerializer<*> {
        if (property != null) {
            val stringReplace = property.getAnnotation(JsonStringReplace::class.java)
                    ?: throw RuntimeException("未注解@" + JsonStringReplace::class.java.name)
            return StringReplaceSerializer(stringReplace.value, stringReplace.replacement)
        }
        return this
    }

    override fun isEmpty(prov: SerializerProvider, value: String): Boolean {
        return !StringUtils.hasText(value)
    }

    override fun serializeWithType(value: String, gen: JsonGenerator, provider: SerializerProvider,
                                   typeSer: TypeSerializer) {
        serialize(value, gen, provider)
    }

    companion object
}