package top.bettercode.summer.web.serializer

import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.BeanProperty
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.JsonDeserializer
import com.fasterxml.jackson.databind.annotation.JacksonStdImpl
import com.fasterxml.jackson.databind.deser.ContextualDeserializer
import top.bettercode.summer.web.resolver.UnitConverter
import top.bettercode.summer.web.resolver.UnitGenericConverter

/**
 * @author Peter Wu
 */
@JacksonStdImpl
class UnitDeserializer @JvmOverloads
constructor(private val value: Int = 100,
            private val scale: Int = 2,
            private val type: Class<out Number> = Long::class.java
) : JsonDeserializer<Number>(), ContextualDeserializer {


    override fun deserialize(p: JsonParser, ctxt: DeserializationContext): Number {
        return UnitGenericConverter.smaller(number = p.toString(), type = type, value = value, scale = scale)
    }


    override fun createContextual(prov: DeserializationContext?, property: BeanProperty?): JsonDeserializer<*> {
        if (property != null) {
            val annotation = property.getAnnotation(UnitConverter::class.java)
                    ?: throw RuntimeException("未注解@" + UnitConverter::class.java.name)

            @Suppress("UNCHECKED_CAST")
            return UnitDeserializer(annotation.value, annotation.scale, property.type.rawClass as Class<out Number>)
        }
        return this
    }
}
