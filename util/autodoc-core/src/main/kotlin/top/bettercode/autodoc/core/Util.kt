package top.bettercode.autodoc.core

import top.bettercode.autodoc.core.model.DocCollection
import top.bettercode.autodoc.core.model.DocCollections
import top.bettercode.autodoc.core.model.Field
import top.bettercode.lang.util.RandomUtil
import com.fasterxml.jackson.annotation.JsonAutoDetect
import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.PropertyAccessor
import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.core.json.JsonReadFeature
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.databind.type.TypeFactory
import com.fasterxml.jackson.dataformat.yaml.YAMLGenerator
import com.fasterxml.jackson.dataformat.yaml.YAMLMapper
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import com.github.stuxuhai.jpinyin.PinyinFormat
import com.github.stuxuhai.jpinyin.PinyinHelper
import org.springframework.util.ClassUtils
import org.springframework.util.MultiValueMap
import java.io.File
import java.util.*
import kotlin.collections.LinkedHashSet


/**
 *
 * @author Peter Wu
 */
object Util {
    val objectMapper = ObjectMapper()
    val yamlMapper = YAMLMapper()

    init {
        init(objectMapper)
        init(yamlMapper)
        yamlMapper.enable(YAMLGenerator.Feature.INDENT_ARRAYS)
        yamlMapper.enable(YAMLGenerator.Feature.ALWAYS_QUOTE_NUMBERS_AS_STRINGS)
        yamlMapper.disable(YAMLGenerator.Feature.WRITE_DOC_START_MARKER)
        yamlMapper.setSerializationInclusion(JsonInclude.Include.NON_EMPTY)

        objectMapper.setSerializationInclusion(JsonInclude.Include.ALWAYS)
    }

    private fun init(objectMapper: ObjectMapper) {
        objectMapper.disable(SerializationFeature.FAIL_ON_EMPTY_BEANS)
        objectMapper.enable(DeserializationFeature.ACCEPT_EMPTY_STRING_AS_NULL_OBJECT)
        objectMapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
        objectMapper.enable(JsonReadFeature.ALLOW_UNESCAPED_CONTROL_CHARS.mappedFeature())
        objectMapper.enable(JsonGenerator.Feature.WRITE_BIGDECIMAL_AS_PLAIN)
        objectMapper.enable(DeserializationFeature.USE_BIG_DECIMAL_FOR_FLOATS)
        objectMapper.registerKotlinModule()
            .setVisibility(PropertyAccessor.FIELD, JsonAutoDetect.Visibility.ANY)
    }
}

fun Any.toJsonString(prettyPrint: Boolean = true): String {
    if (this is String) {
        return this
    }
    return if (prettyPrint) {
        Util.objectMapper.enable(SerializationFeature.INDENT_OUTPUT).writeValueAsString(this)
    } else {
        Util.objectMapper.disable(SerializationFeature.INDENT_OUTPUT).writeValueAsString(this)
    }
}

fun <T> File.parseList(clazz: Class<T>): LinkedHashSet<T> {
    return if (exists() && length() > 0) {
        return try {
            val
                    collectionType = TypeFactory.defaultInstance()
                .constructCollectionType(LinkedHashSet::class.java, clazz)
            val set = Util.yamlMapper.readValue<LinkedHashSet<T>>(this, collectionType)
                .filterNotNull()
            LinkedHashSet(set)
        } catch (e: Exception) {
            println("$this>>${e.message}")
            linkedSetOf()
        }
    } else {
        linkedSetOf()
    }
}

val MultiValueMap<String, String>.singleValueMap: Map<String, String>
    get() {
        return this.mapValues { it.value.joinToString(",") }
    }

val Any.type: String
    get() {
        if (this::class.java == String::class.java) {
            return "String"
        } else if (ClassUtils.isPrimitiveOrWrapper(this::class.java)) {
            return this::class.java.simpleName
        } else if (this::class.java.isArray || (Collection::class.java.isAssignableFrom(this::class.java) && !Map::class.java.isAssignableFrom(
                this::class.java
            ))
        ) {
            return "Array"
        }
        return "Object"
    }

/**
 * 尝试转换字符串为对象
 */
fun Any.toMap(): Map<String, Any?>? {
    val result = this.convert()
    if (result is Map<*, *>) {
        @Suppress("UNCHECKED_CAST")
        return result as? Map<String, Any?>
    }
    return null
}


/**
 * @param unwrapped 解析字段
 */
fun Any.convert(unwrapped: Boolean = true): Any? {
    if (this is List<*> && this.isNotEmpty()) {
        return if (unwrapped) {
            val map = mutableMapOf<Any?, Any?>()
            this.forEach {
                val convertAny = it?.convert(unwrapped)
                if (convertAny is Map<*, *>) {
                    convertAny.forEach { (any, u) ->
                        val value = map[any]
                        if (value == null) {
                            map[any] = u
                        }
                        if (!isEmpty(u) && isEmpty(value)) {
                            map[any] = u
                        }
                    }
                }
            }
            if (map.isEmpty()) {
                this.firstOrNull { it != null }?.convert(unwrapped)
            } else
                map
        } else
            this
    } else if (this is Map<*, *>) {
        return this
    } else if (this is String) {
        return if (this.isBlank()) {
            this
        } else {
            try {
                Util.objectMapper.readValue(this, Map::class.java)
            } catch (ignore: Exception) {
                try {
                    Util.objectMapper.readValue(this, List::class.java).convert(unwrapped)
                } catch (e: Exception) {
                    return this
                }
            }
        }
    }
    return this
}

private fun isEmpty(value: Any?) =
    value == null || (value is Collection<*> && value.isEmpty()) || (value is Array<*> && value.isEmpty())

internal fun File.readCollections(): LinkedHashSet<DocCollection> {
    return if (exists() && length() > 0) Util.yamlMapper.readValue(
        this.inputStream(),
        DocCollections::class.java
    ).mapTo(linkedSetOf()) { (k, v) ->
        DocCollection(k, LinkedHashSet(v), File(this.parentFile, "collection/${k}"))
    } else linkedSetOf()
}

internal fun File.writeCollections(collections: LinkedHashSet<DocCollection>) {
    this.writeText("")
    collections.forEach { collection ->
        this.appendText("\"${collection.name}\":\n")
        collection.items.forEach {
            this.appendText("  - \"$it\"\n")
        }
    }
}

fun MutableMap<String, Int>.pyname(name: String): String {
    var pyname =
        PinyinHelper.convertToPinyinString(name, "", PinyinFormat.WITHOUT_TONE)
            .lowercase(Locale.getDefault())
            .replace("[^\\x00-\\xff]|[()\\[\\]{}|/]|\\s*|\t|\r|\n".toRegex(), "")
    val no = this[pyname]
    if (no != null) {
        val i = no + 1
        this[pyname] = i
        pyname += "_${RandomUtil.nextString(2).lowercase(Locale.getDefault())}_$i"
    } else
        this[pyname] = 0
    return pyname
}


fun Set<Field>.checkBlank(desc: String, prefix: String = ""): Set<Field> {
    forEach {
        val blank = it.description.isBlank()
        if (blank) {
            System.err.println("[${desc}]未找到字段[${prefix + it.name}]的描述")
        }
        it.children.checkBlank(desc, "${prefix + it.name}.")
    }
    return this
}

