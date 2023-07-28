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
import java.util.stream.Collectors
import java.util.stream.Stream

/**
 * 增加 图片即时处理 链接
 *
 * @author Peter Wu
 */
@JacksonStdImpl
class QvodImageProcessSerializer @JvmOverloads constructor(
        private val picTemplateId: String = "",
        private val separator: String = ""
) : StdScalarSerializer<Any>(
        Any::class.java, false
), ContextualSerializer {


    private val qvodClient: QvodClient = ApplicationContextHolder.getBean(QvodClient::class.java)!!

    override fun serialize(value: Any, gen: JsonGenerator, provider: SerializerProvider) {
        gen.writeObject(value)

        val fieldName = gen.outputContext.currentName
        if (value is String) {
            if (separator.isEmpty()) {
                gen.writeStringField("${fieldName}Thumb", process(value))
            } else {
                val split =
                        value.split(separator.toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
                genCollection(gen, fieldName, Arrays.stream(split))
            }
        } else if (value is Array<*>) {
            genCollection(gen, fieldName, Arrays.stream(value))
        } else if (value is Collection<*>) {
            genCollection(gen, fieldName, value.stream())
        } else {
            throw UnsupportedOperationException()
        }
    }

    private fun process(value: Any) =
            "$value!${picTemplateId.ifBlank { qvodClient.properties.picTemplateId }}.jpg"

    private fun genCollection(
            gen: JsonGenerator, fieldName: String, stream: Stream<*>
    ) {
        val urls = stream.map { it?.toString()?.trim() }
                .filter { !it.isNullOrBlank() }
                .map { process(it!!) }.collect(Collectors.toList())
        val urlFieldName = fieldName + "Thumb"
        gen.writeObjectField(urlFieldName, urls)
    }

    override fun serializeWithType(
            value: Any, gen: JsonGenerator, provider: SerializerProvider,
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
                    annotation.value, annotation.separator
            )
        }
        return this
    }

    companion object {
        private const val serialVersionUID: Long = 1L
    }
}