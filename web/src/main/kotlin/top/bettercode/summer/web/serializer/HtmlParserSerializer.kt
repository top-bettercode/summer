package top.bettercode.summer.web.serializer

import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.databind.BeanProperty
import com.fasterxml.jackson.databind.JsonMappingException
import com.fasterxml.jackson.databind.JsonSerializer
import com.fasterxml.jackson.databind.SerializerProvider
import com.fasterxml.jackson.databind.annotation.JacksonStdImpl
import com.fasterxml.jackson.databind.jsontype.TypeSerializer
import com.fasterxml.jackson.databind.ser.ContextualSerializer
import com.fasterxml.jackson.databind.ser.std.StdScalarSerializer
import org.springframework.util.StringUtils
import top.bettercode.summer.tools.lang.util.HtmlUtil.parseHtml
import top.bettercode.summer.tools.lang.util.HtmlUtil.subParseHtml
import top.bettercode.summer.web.serializer.annotation.JsonHtmlParser
import java.io.IOException

/**
 * @author Peter Wu
 */
@JacksonStdImpl
class HtmlParserSerializer : StdScalarSerializer<String>, ContextualSerializer {
    private val length: Int

    constructor() : super(String::class.java, false) {
        length = -1
    }

    constructor(length: Int) : super(String::class.java, false) {
        this.length = length
    }

    override fun serialize(value: String, gen: JsonGenerator, provider: SerializerProvider) {
        var v: String? = value
        if (StringUtils.hasText(v)) {
            v = if (length == -1) {
                parseHtml(v)
            } else {
                subParseHtml(v, length)
            }
        }
        gen.writeString(v)
    }

    override fun createContextual(prov: SerializerProvider, property: BeanProperty?): JsonSerializer<*> {
        if (property != null && String::class.java.isAssignableFrom(property.type.rawClass)) {
            val htmlParser = property.getAnnotation(JsonHtmlParser::class.java)
            val length = htmlParser?.value ?: -1
            return HtmlParserSerializer(length)
        }
        return prov.findNullValueSerializer(property)
    }

    override fun isEmpty(prov: SerializerProvider, value: String): Boolean {
        return !StringUtils.hasText(value)
    }

    override fun serializeWithType(value: String, gen: JsonGenerator, provider: SerializerProvider,
                                   typeSer: TypeSerializer) {
        serialize(value, gen, provider)
    }

    companion object {
        private const val serialVersionUID = 1L
    }
}