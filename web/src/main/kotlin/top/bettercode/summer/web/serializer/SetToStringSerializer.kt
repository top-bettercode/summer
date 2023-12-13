package top.bettercode.summer.web.serializer

import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.databind.BeanProperty
import com.fasterxml.jackson.databind.JsonSerializer
import com.fasterxml.jackson.databind.SerializerProvider
import com.fasterxml.jackson.databind.annotation.JacksonStdImpl
import com.fasterxml.jackson.databind.jsontype.TypeSerializer
import com.fasterxml.jackson.databind.ser.ContextualSerializer
import com.fasterxml.jackson.databind.ser.std.StdScalarSerializer
import top.bettercode.summer.web.serializer.annotation.JsonSetToString

/**
 * JSON序列化url自动补全
 *
 * @author Peter Wu
 */
@JacksonStdImpl
class SetToStringSerializer @JvmOverloads constructor(
        private val useExtensionField: Boolean = true,
        private val separator: String = ",") : StdScalarSerializer<Set<Any>>(Set::class.java, false), ContextualSerializer {
    override fun serialize(value: Set<Any>, gen: JsonGenerator, provider: SerializerProvider) {
        if (useExtensionField) {
            gen.writeObject(value)
            val outputContext = gen.outputContext
            val fieldName = outputContext.currentName
            gen.writeObjectField(fieldName + "Ext", value.joinToString(separator))
        } else {
            gen.writeString(value.joinToString(separator))
        }
    }

    override fun serializeWithType(value: Set<Any>, gen: JsonGenerator, provider: SerializerProvider,
                                   typeSer: TypeSerializer) {
        serialize(value, gen, provider)
    }

    override fun createContextual(prov: SerializerProvider, property: BeanProperty?): JsonSerializer<*> {
        if (property != null) {
            val annotation = property.getAnnotation(JsonSetToString::class.java)
                    ?: throw RuntimeException("未注解@" + JsonSetToString::class.java.name)
            return SetToStringSerializer(annotation.extended, annotation.value)
        }
        return this
    }

}
