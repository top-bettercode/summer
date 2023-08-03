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
import top.bettercode.summer.web.serializer.annotation.JsonArray

/**
 * JSON序列化url自动补全
 *
 * @author Peter Wu
 */
@JacksonStdImpl
class ArraySerializer @JvmOverloads constructor(
        private val useExtensionField: Boolean = true,
        private val separator: String = ",") : StdScalarSerializer<String>(String::class.java, false), ContextualSerializer {
    override fun serialize(value: String, gen: JsonGenerator, provider: SerializerProvider) {
        if (useExtensionField) {
            gen.writeString(value)
            val outputContext = gen.outputContext
            val fieldName = outputContext.currentName
            gen.writeObjectField(fieldName + "Array",
                    if (StringUtils.hasText(value)) value.split(separator.toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray() else arrayOf(0))
        } else {
            gen.writeObject(
                    if (StringUtils.hasText(value)) value.split(separator.toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray() else arrayOf(0))
        }
    }

    override fun serializeWithType(value: String, gen: JsonGenerator, provider: SerializerProvider,
                                   typeSer: TypeSerializer) {
        serialize(value, gen, provider)
    }

    override fun createContextual(prov: SerializerProvider, property: BeanProperty?): JsonSerializer<*> {
        if (property != null) {
            val annotation = property.getAnnotation(JsonArray::class.java)
                    ?: throw RuntimeException("未注解@" + JsonArray::class.java.name)
            return ArraySerializer(annotation.extended, annotation.value)
        }
        return this
    }

}
