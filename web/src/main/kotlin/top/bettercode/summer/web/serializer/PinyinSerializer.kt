package top.bettercode.summer.web.serializer

import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.databind.BeanProperty
import com.fasterxml.jackson.databind.JsonSerializer
import com.fasterxml.jackson.databind.SerializerProvider
import com.fasterxml.jackson.databind.annotation.JacksonStdImpl
import com.fasterxml.jackson.databind.jsontype.TypeSerializer
import com.fasterxml.jackson.databind.ser.ContextualSerializer
import com.fasterxml.jackson.databind.ser.std.StdScalarSerializer
import com.github.stuxuhai.jpinyin.PinyinHelper
import org.slf4j.LoggerFactory
import top.bettercode.summer.web.serializer.annotation.JsonPinyin

/**
 * @author Peter Wu
 */
@JacksonStdImpl
class PinyinSerializer @JvmOverloads constructor(
    private val separator: String = "",
    private val format: PinyinFormat = PinyinFormat.WITHOUT_TONE
) : StdScalarSerializer<String>(String::class.java, false), ContextualSerializer {
    private val log = LoggerFactory.getLogger(PinyinSerializer::class.java)
    override fun serialize(value: String, gen: JsonGenerator, provider: SerializerProvider) {
        gen.writeString(value)
        val outputContext = gen.outputContext
        val fieldName = outputContext.currentName
        gen.writeFieldName(fieldName + "Pinyin")
        val pinyinFormat = when (format) {
            PinyinFormat.WITH_TONE_MARK -> com.github.stuxuhai.jpinyin.PinyinFormat.WITH_TONE_MARK
            PinyinFormat.WITHOUT_TONE -> com.github.stuxuhai.jpinyin.PinyinFormat.WITHOUT_TONE
            PinyinFormat.WITH_TONE_NUMBER -> com.github.stuxuhai.jpinyin.PinyinFormat.WITH_TONE_NUMBER
            PinyinFormat.SHORT -> {
                gen.writeString(PinyinHelper.getShortPinyin(value))
                return
            }
        }
        gen.writeString(PinyinHelper.convertToPinyinString(value, separator, pinyinFormat))
    }

    override fun createContextual(
        prov: SerializerProvider,
        property: BeanProperty?
    ): JsonSerializer<*> {
        if (property != null) {
            val annotation = property.getAnnotation(JsonPinyin::class.java)
            return PinyinSerializer(annotation.separator, annotation.format)
        }
        return prov.findNullValueSerializer(null)
    }

    override fun isEmpty(prov: SerializerProvider, value: String): Boolean {
        return value.isEmpty()
    }

    override fun serializeWithType(
        value: String, gen: JsonGenerator, provider: SerializerProvider,
        typeSer: TypeSerializer
    ) {
        serialize(value, gen, provider)
    }

}
