package top.bettercode.summer.tools.qvod

import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.databind.BeanProperty
import com.fasterxml.jackson.databind.JsonSerializer
import com.fasterxml.jackson.databind.SerializerProvider
import com.fasterxml.jackson.databind.annotation.JacksonStdImpl
import com.fasterxml.jackson.databind.jsontype.TypeSerializer
import com.fasterxml.jackson.databind.ser.ContextualSerializer
import com.fasterxml.jackson.databind.ser.std.StdScalarSerializer
import top.bettercode.summer.web.support.ApplicationContextHolder
import java.io.IOException
import java.util.*

/**
 * 增加 图片即时处理 链接
 *
 * @author Peter Wu
 */
@JacksonStdImpl
class QvodImageProcessSerializer @JvmOverloads constructor(
    private val picTemplateId: String = "",
) : StdScalarSerializer<String>(
    String::class.java, false
), ContextualSerializer {


    private val qvodClient: QvodClient = ApplicationContextHolder.getBean(QvodClient::class.java)

    @Throws(IOException::class)
    override fun serialize(value: String, gen: JsonGenerator, provider: SerializerProvider) {
        gen.writeObject(value)

        val fieldName = gen.outputContext.currentName
        gen.writeStringField(
            "${fieldName}Thumb",
            "$value!${picTemplateId.ifBlank { qvodClient.properties.picTemplateId }}.jpg"
        )

    }

    @Throws(IOException::class)
    override fun serializeWithType(
        value: String, gen: JsonGenerator, provider: SerializerProvider,
        typeSer: TypeSerializer
    ) {
        serialize(value, gen, provider)
    }

    override fun createContextual(
        prov: SerializerProvider,
        property: BeanProperty?
    ): JsonSerializer<*> {
        if (property != null) {
            val annotation = property.getAnnotation(QvodImageProcess::class.java)
                ?: throw RuntimeException("未注解@" + QvodImageProcess::class.java.name)
            return QvodImageProcessSerializer(
                annotation.value
            )
        }
        return this
    }

    companion object {
        private const val serialVersionUID: Long = 1L
    }
}