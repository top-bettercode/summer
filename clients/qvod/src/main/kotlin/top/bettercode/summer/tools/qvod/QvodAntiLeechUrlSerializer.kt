package top.bettercode.summer.tools.qvod

import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.databind.BeanProperty
import com.fasterxml.jackson.databind.JsonSerializer
import com.fasterxml.jackson.databind.SerializerProvider
import com.fasterxml.jackson.databind.annotation.JacksonStdImpl
import com.fasterxml.jackson.databind.jsontype.TypeSerializer
import com.fasterxml.jackson.databind.ser.ContextualSerializer
import com.fasterxml.jackson.databind.ser.std.StdScalarSerializer
import top.bettercode.summer.web.serializer.JsonUrlMapper
import top.bettercode.summer.web.support.ApplicationContextHolder
import java.util.*
import java.util.stream.Collectors
import java.util.stream.Stream
import kotlin.reflect.KClass
import kotlin.reflect.full.primaryConstructor

/**
 * 防盗链URL 处理
 *
 * @author Peter Wu
 */
@JacksonStdImpl
class QvodAntiLeechUrlSerializer @JvmOverloads constructor(
        private val separator: String = "",
        private val mapperType: KClass<out JsonUrlMapper>? = null
) : StdScalarSerializer<Any>(
        Any::class.java, false
), ContextualSerializer {

    private val mapper: JsonUrlMapper
        get() = if (mapperType == null || mapperType == JsonUrlMapper::class.java) {
            defaultMapper
        } else {
            var jsonUrlMapper = mapperCache[mapperType]
            if (jsonUrlMapper == null) {
                jsonUrlMapper = mapperType.primaryConstructor!!.call()
                mapperCache[mapperType] = jsonUrlMapper
            }
            jsonUrlMapper
        }

    private val qvodClient: QvodClient = ApplicationContextHolder.getBean(QvodClient::class.java)!!

    override fun serialize(value: Any, gen: JsonGenerator, provider: SerializerProvider) {
        val fieldName = gen.outputContext.currentName
        val urlFieldName = fieldName + "Alurl"
        if (value is String) {
            var url: String = value
            if (separator.isEmpty()) {
                url = mapper.mapper(url)!!
                gen.writeObject(url)
                gen.writeStringField(urlFieldName, qvodClient.antiLeechUrl(url))
            } else {
                val split =
                        value.split(separator.toRegex()).dropLastWhile { it.isEmpty() }
                genCollection(value, gen, fieldName, split.stream())
            }
        } else if (value is Array<*>) {
            genCollection(value, gen, fieldName, Arrays.stream(value))
        } else if (value is Collection<*>) {
            genCollection(value, gen, fieldName, value.stream())
        } else {
            throw UnsupportedOperationException()
        }
    }

    private fun genCollection(
            value: Any, gen: JsonGenerator, fieldName: String, stream: Stream<*>
    ) {
        val urls = stream.map { mapper.mapper(it) }
                .filter { !it.isNullOrBlank() }
                .map { qvodClient.antiLeechUrl(it!!) }.collect(Collectors.toList())
        gen.writeObject(value)
        val urlFieldName = fieldName + "Alurls"
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
            val annotation = property.getAnnotation(QvodAntiLeechUrl::class.java)
                    ?: throw RuntimeException("未注解@" + QvodAntiLeechUrl::class.java.name)
            return QvodAntiLeechUrlSerializer(
                    annotation.separator
            )
        }
        return this
    }

    companion object {

        private val defaultMapper: JsonUrlMapper = object :
                JsonUrlMapper {}
        private val mapperCache: MutableMap<KClass<out JsonUrlMapper>, JsonUrlMapper?> = HashMap()
    }
}