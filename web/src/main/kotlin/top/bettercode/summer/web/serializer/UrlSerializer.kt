package top.bettercode.summer.web.serializer

import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.databind.BeanProperty
import com.fasterxml.jackson.databind.JsonSerializer
import com.fasterxml.jackson.databind.SerializerProvider
import com.fasterxml.jackson.databind.annotation.JacksonStdImpl
import com.fasterxml.jackson.databind.jsontype.TypeSerializer
import com.fasterxml.jackson.databind.ser.ContextualSerializer
import com.fasterxml.jackson.databind.ser.std.StdScalarSerializer
import org.springframework.core.env.Environment
import org.springframework.util.StringUtils
import org.springframework.web.context.request.RequestContextHolder
import org.springframework.web.context.request.ServletRequestAttributes
import top.bettercode.summer.tools.lang.operation.RequestConverter.getRequestPath
import top.bettercode.summer.web.serializer.annotation.JsonUrl
import java.lang.reflect.InvocationTargetException
import java.util.*
import java.util.stream.Collectors
import java.util.stream.Stream
import kotlin.collections.set
import kotlin.reflect.KClass
import kotlin.reflect.full.primaryConstructor

/**
 * JSON序列化url自动补全
 *
 * @author Peter Wu
 */
@JacksonStdImpl
class UrlSerializer @JvmOverloads constructor(private val formatExpression: String? = null, private var urlFieldName: String = "",
                                              private val useExtensionField: Boolean = true,
                                              private val asMap: Boolean = true,
                                              private val separator: String = "",
                                              private val defaultValue: String = "",
                                              private val mapperType: KClass<out JsonUrlMapper> = JsonUrlMapper::class) : StdScalarSerializer<Any>(Any::class.java, false), ContextualSerializer {
    private val mapper: JsonUrlMapper
        get() = if (mapperType == JsonUrlMapper::class) {
            defaultMapper
        } else {
            var jsonUrlMapper = mapperCache[mapperType]
            if (jsonUrlMapper == null) {
                try {
                    jsonUrlMapper = mapperType.primaryConstructor!!.call()
                    mapperCache[mapperType] = jsonUrlMapper
                } catch (e: InstantiationException) {
                    throw RuntimeException("mapper实例化失败", e)
                } catch (e: IllegalAccessException) {
                    throw RuntimeException("mapper实例化失败", e)
                } catch (e: NoSuchMethodException) {
                    throw RuntimeException("mapper实例化失败", e)
                } catch (e: InvocationTargetException) {
                    throw RuntimeException("mapper实例化失败", e)
                }
            }
            jsonUrlMapper
        }

    override fun serialize(value: Any, gen: JsonGenerator, provider: SerializerProvider) {
        var v = value
        val type: Class<*> = v.javaClass
        val mapper = mapper
        if (type == String::class.java) {
            if (!StringUtils.hasText(v as String)) {
                v = defaultValue
            }
            if (separator.isEmpty()) {
                val path = mapper.mapper(v)
                if (useExtensionField) {
                    val outputContext = gen.outputContext
                    val fieldName = outputContext.currentName
                    gen.writeString(path)
                    if ("" == urlFieldName) {
                        urlFieldName = fieldName + "Url"
                    }
                    gen.writeStringField(urlFieldName, convert(path, formatExpression))
                } else {
                    gen.writeString(convert(path, formatExpression))
                }
            } else {
                val path = v
                val split = path.split(separator.toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
                genCollection(path, gen, mapper, Arrays.stream(split))
            }
        } else if (type.isArray) {
            val array = v as Array<*>
            genCollection(v, gen, mapper, Arrays.stream(array))
        } else if (Collection::class.java.isAssignableFrom(type)) {
            val array = v as Collection<*>
            genCollection(v, gen, mapper, array.stream())
        } else {
            throw UnsupportedOperationException()
        }
    }

    private fun genCollection(value: Any, gen: JsonGenerator, mapper: JsonUrlMapper?,
                              stream: Stream<*>) {
        val urls = stream.map { obj: Any? -> mapper!!.mapper(obj) }.filter { str: String? -> StringUtils.hasText(str) }
                .map { s: String? ->
                    if (asMap) {
                        return@map PathUrl(s, convert(s, formatExpression))
                    } else {
                        return@map convert(s, formatExpression)
                    }
                }.collect(Collectors.toList())
        if (useExtensionField) {
            gen.writeObject(value)
            val outputContext = gen.outputContext
            val fieldName = outputContext.currentName
            if ("" == urlFieldName) {
                urlFieldName = fieldName + "Urls"
            }
            gen.writeObjectField(urlFieldName, urls)
        } else {
            gen.writeObject(urls)
        }
    }

    override fun serializeWithType(value: Any, gen: JsonGenerator, provider: SerializerProvider,
                                   typeSer: TypeSerializer) {
        serialize(value, gen, provider)
    }

    override fun createContextual(prov: SerializerProvider, property: BeanProperty?): JsonSerializer<*> {
        if (property != null) {
            val annotation = property.getAnnotation(JsonUrl::class.java)
                    ?: throw RuntimeException("未注解@" + JsonUrl::class.java.name)
            return UrlSerializer(annotation.value, annotation.urlFieldName, annotation.extended,
                    annotation.asMap,
                    annotation.separator, annotation.defaultValue, annotation.mapper)
        }
        return this
    }

    companion object {
        private const val serialVersionUID = 1L
        private var defaultFormatExpression = "\${summer.multipart.file-url-format}"
        private val defaultMapper: JsonUrlMapper = object : JsonUrlMapper {}
        private var environment: Environment? = null
        private val formatCache: MutableMap<String, String?> = HashMap()
        private val mapperCache: MutableMap<KClass<out JsonUrlMapper>, JsonUrlMapper?> = HashMap()
        private var defaultFormat: String? = null
        fun setDefaultFormatExpression(defaultFormatExpression: String) {
            Companion.defaultFormatExpression = defaultFormatExpression
        }

        @JvmStatic
        fun setEnvironment(environment: Environment) {
            Companion.environment = environment
            defaultFormat = environment.resolvePlaceholders(defaultFormatExpression)
            if (defaultFormat?.contains("%s") != true) {
                defaultFormat = "$defaultFormat%s"
            }
        }

        @JvmStatic
        @JvmOverloads
        fun convert(path: String?, formatExpression: String? = null): String? {
            return if (StringUtils.hasText(path)) {
                if (path!!.startsWith("http://") || path.startsWith("https://")) {
                    return path
                }
                var format: String?
                if (StringUtils.hasText(formatExpression)) {
                    format = formatCache[formatExpression]
                    if (format == null) {
                        format = environment!!.resolvePlaceholders(formatExpression!!)
                        if (!format.contains("%s")) {
                            format = "$format%s"
                        }
                        formatCache[formatExpression] = format
                    }
                } else {
                    format = defaultFormat
                }
                var url = String.format(format!!, path)
                if (!url.startsWith("http://") && !url.startsWith("https://")) {
                    val requestAttributes = RequestContextHolder
                            .getRequestAttributes() as ServletRequestAttributes?
                    if (requestAttributes != null) {
                        val request = requestAttributes.request
                        val requestPath = getRequestPath(request)
                        if (StringUtils.hasText(requestPath)) {
                            url = String.format("%s%s", requestPath, url)
                        }
                    }
                }
                url
            } else {
                path
            }
        }
    }
}
