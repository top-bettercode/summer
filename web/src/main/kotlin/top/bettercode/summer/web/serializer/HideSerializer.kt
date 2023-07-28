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
import top.bettercode.summer.web.serializer.annotation.JsonHide
import java.util.*

/**
 * JSON 序列化电话号码自动隐藏
 *
 * @author Peter Wu
 */
@JacksonStdImpl
class HideSerializer @JvmOverloads constructor(private val beginKeep: Int = 0, private val endKeep: Int = 0, private val alwaysHide: Boolean = true) : StdScalarSerializer<String>(String::class.java, false), ContextualSerializer {
    fun convert(value: String): String {
        if (StringUtils.hasText(value)) {
            val keep = beginKeep + endKeep
            if (value.length > keep) {
                val chars = value.toCharArray()
                var i = 0
                while (i < chars.size) {
                    if (i >= beginKeep && i < chars.size - endKeep) {
                        chars[i] = '*'
                    }
                    i++
                }
                return String(chars)
            } else if (alwaysHide) {
                val chars = CharArray(value.length)
                Arrays.fill(chars, '*')
                return String(chars)
            }
        }
        return value
    }

    override fun serialize(value: String, gen: JsonGenerator, provider: SerializerProvider) {
        gen.writeString(convert(value))
    }

    override fun createContextual(prov: SerializerProvider, property: BeanProperty?): JsonSerializer<*> {
        if (property != null) {
            val jsonHide = property.getAnnotation(JsonHide::class.java)
                    ?: throw RuntimeException("未注解@" + JsonHide::class.java.name)
            return HideSerializer(jsonHide.beginKeep, jsonHide.endKeep, jsonHide.alwaysHide)
        }
        return this
    }

    override fun isEmpty(prov: SerializerProvider, value: String): Boolean {
        return !StringUtils.hasText(value)
    }

    override fun serializeWithType(value: String?, gen: JsonGenerator, provider: SerializerProvider,
                                   typeSer: TypeSerializer) {
        gen.writeString(value)
    }

    companion object {
        private const val serialVersionUID = 1L
    }
}
