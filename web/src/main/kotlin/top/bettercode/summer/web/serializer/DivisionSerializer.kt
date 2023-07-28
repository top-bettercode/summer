package top.bettercode.summer.web.serializer

import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.databind.BeanProperty
import com.fasterxml.jackson.databind.JsonSerializer
import com.fasterxml.jackson.databind.SerializerProvider
import com.fasterxml.jackson.databind.annotation.JacksonStdImpl
import com.fasterxml.jackson.databind.jsontype.TypeSerializer
import com.fasterxml.jackson.databind.ser.ContextualSerializer
import com.fasterxml.jackson.databind.ser.std.StdScalarSerializer
import org.slf4j.LoggerFactory
import top.bettercode.summer.web.serializer.annotation.JsonDivision
import top.bettercode.summer.web.support.gb2260.GB2260.getDivision
import top.bettercode.summer.web.support.gb2260.InvalidCodeException

/**
 * @author Peter Wu
 */
@JacksonStdImpl
class DivisionSerializer @JvmOverloads constructor(private val vnode: Boolean = false) : StdScalarSerializer<String>(String::class.java, false), ContextualSerializer {
    private val log = LoggerFactory.getLogger(DivisionSerializer::class.java)
    override fun serialize(value: String, gen: JsonGenerator, provider: SerializerProvider) {
        gen.writeString(value)
        val outputContext = gen.outputContext
        val fieldName = outputContext.currentName
        gen.writeFieldName(fieldName + "Path")
        try {
            val division = getDivision(value)
            gen.writeObject(division.codes(vnode))
        } catch (e: InvalidCodeException) {
            log.warn(e.message)
            gen.writeNull()
        }
    }

    override fun createContextual(prov: SerializerProvider, property: BeanProperty?): JsonSerializer<*> {
        if (property != null) {
            val annotation = property.getAnnotation(JsonDivision::class.java)
            return DivisionSerializer(annotation.value)
        }
        return prov.findNullValueSerializer(null)
    }

    override fun isEmpty(prov: SerializerProvider, value: String): Boolean {
        return value.isEmpty()
    }

    override fun serializeWithType(value: String, gen: JsonGenerator, provider: SerializerProvider,
                                   typeSer: TypeSerializer) {
        serialize(value, gen, provider)
    }

    companion object {
        private const val serialVersionUID = 1L
    }
}
