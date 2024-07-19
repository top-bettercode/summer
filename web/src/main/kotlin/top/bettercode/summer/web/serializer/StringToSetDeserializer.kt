package top.bettercode.summer.web.serializer

import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.core.JsonToken
import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.BeanProperty
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.JavaType
import com.fasterxml.jackson.databind.JsonDeserializer
import com.fasterxml.jackson.databind.annotation.JacksonStdImpl
import com.fasterxml.jackson.databind.deser.ContextualDeserializer
import com.fasterxml.jackson.databind.deser.std.StdScalarDeserializer
import com.fasterxml.jackson.databind.jsontype.TypeDeserializer
import com.fasterxml.jackson.databind.type.TypeFactory
import top.bettercode.summer.web.serializer.annotation.JsonSetToString

/**
 * JSON序列化url自动补全
 *
 * @author Peter Wu
 */
@JacksonStdImpl
class StringToSetDeserializer @JvmOverloads constructor(
        private val useExtensionField: Boolean = true,
        private val separator: String = ",") : StdScalarDeserializer<Set<String>>(Set::class.java), ContextualDeserializer {

    private val valueType = TypeFactory.defaultInstance().constructType(String::class.java)

    override fun getValueType(): JavaType {
        return valueType
    }


    override fun deserialize(jp: JsonParser, ctxt: DeserializationContext?): Set<String> {
        val token: JsonToken = jp.currentToken
        if (!useExtensionField && token.isScalarValue) {
            return jp.text.split(separator).toSet()
        }
        return jp.readValueAs(object : TypeReference<Set<String?>?>() {})
    }

    override fun deserializeWithType(p: JsonParser, ctxt: DeserializationContext?, typeDeserializer: TypeDeserializer?, intoValue: Set<String>?): Set<String> {
        return deserialize(p, ctxt)
    }

    override fun createContextual(ctxt: DeserializationContext?, property: BeanProperty?): JsonDeserializer<*> {
        if (property != null) {
            val annotation = property.getAnnotation(JsonSetToString::class.java)
                    ?: throw RuntimeException("未注解@" + JsonSetToString::class.java.name)
            return StringToSetDeserializer(annotation.extended, annotation.value)
        }
        return this
    }

}
