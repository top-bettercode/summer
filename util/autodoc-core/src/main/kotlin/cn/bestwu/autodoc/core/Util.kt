package cn.bestwu.autodoc.core

import cn.bestwu.autodoc.core.model.DocCollection
import cn.bestwu.autodoc.core.model.DocCollections
import cn.bestwu.autodoc.core.model.Field
import cn.bestwu.lang.util.RandomUtil
import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.databind.type.TypeFactory
import com.fasterxml.jackson.dataformat.yaml.YAMLGenerator
import com.fasterxml.jackson.dataformat.yaml.YAMLMapper
import com.github.stuxuhai.jpinyin.PinyinFormat
import com.github.stuxuhai.jpinyin.PinyinHelper
import org.springframework.util.ClassUtils
import org.springframework.util.MultiValueMap
import java.io.File


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
    }

    private fun init(objectMapper: ObjectMapper) {
        objectMapper.disable(SerializationFeature.FAIL_ON_EMPTY_BEANS)
        objectMapper.setSerializationInclusion(JsonInclude.Include.ALWAYS)
        objectMapper.enable(DeserializationFeature.ACCEPT_EMPTY_STRING_AS_NULL_OBJECT)
        objectMapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
        objectMapper.enable(JsonParser.Feature.ALLOW_UNQUOTED_CONTROL_CHARS)
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
                    collectionType = TypeFactory.defaultInstance().constructCollectionType(LinkedHashSet::class.java, clazz)
            var set = Util.yamlMapper.readValue<LinkedHashSet<T>>(this, collectionType)
            set = LinkedHashSet(set.filter { it != null })
            set
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
        } else if (this::class.java.isArray || (Collection::class.java.isAssignableFrom(this::class.java) && !Map::class.java.isAssignableFrom(this::class.java))) {
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
    return if (exists() && length() > 0) Util.yamlMapper.readValue(this.inputStream(), DocCollections::class.java).mapTo(linkedSetOf()) { (k, v) ->
        DocCollection(k, LinkedHashSet(v))
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

fun Set<Field>.knewFixFields(needFixFields: Set<Field>) {
    needFixFields.forEach {
        fixField(it, true)
    }
}

fun Set<Field>.fixField(it: Field, hasDesc: Boolean = false, coverType: Boolean = true) {
    val findField = this.findPossibleField(it.name, it.value.type, hasDesc)
    if (findField != null && (it.description.isBlank() || !findField.canCover)) {
        it.canCover = findField.canCover
        it.defaultVal = findField.defaultVal
        if (coverType || !findField.canCover)
            it.type = findField.type
        if (findField.description.isNotBlank())
            it.description = findField.description

        var tempVal = it.value
        if (tempVal.isBlank()) {
            tempVal = if (findField.value.isBlank()) it.defaultVal else findField.value
        }
        it.value = tempVal.convert(false)?.toJsonString(false) ?: ""
    }
}

fun MutableMap<String, Int>.pyname(name: String): String {
    var pyname = PinyinHelper.convertToPinyinString(name, "", PinyinFormat.WITHOUT_TONE).replace("[^\\x00-\\xff]".toRegex(), "").replace("\\s*|\t|\r|\n".toRegex(), "")
    val no = this[pyname]
    if (no != null) {
        val i = no + 1
        this[pyname] = i
        pyname += "_${RandomUtil.nextString(2).toLowerCase()}_$i"
    } else
        this[pyname] = 0
    return pyname
}

fun Set<Field>.findPossibleField(name: String, type: String, hasDesc: Boolean = false): Field? {
    var field = this.findField(name, type, hasDesc)
    if (field == null) {
        if (name.contains(".")) {
            val newName = name.substring(name.lastIndexOf(".") + 1)
            field = this.findField(newName, type, hasDesc)
            if (field != null) {
                field.name = name
            } else if (newName.endsWith("Name") || newName.endsWith("Url") || newName.endsWith("Urls") || newName.endsWith("Path")) {
                field = this.findFuzzyField(newName, type, hasDesc)
                if (field != null)
                    field.name = name
            }
        } else if ((name.endsWith("Name") || name.endsWith("Url") || name.endsWith("Urls") || name.endsWith("Path")))
            field = this.findFuzzyField(name, type, hasDesc)
    }
    return field
}

private fun Set<Field>.findFuzzyField(name: String, type: String, hasDesc: Boolean = false): Field? {
    val newName = when {
        name.endsWith("Name") -> name.substringBeforeLast("Name")
        name.endsWith("Url") -> name.substringBeforeLast("Url")
        name.endsWith("Urls") -> name.substringBeforeLast("Urls")
        name.endsWith("Path") -> name.substringBeforeLast("Path")
        else -> {
            return null
        }
    }
    val field = this.findField(newName, type, hasDesc)
    if (field != null) {
        field.name = name
        field.description = field.description.split(Regex("[（(,:，：]"))[0]
    }
    return field
}

private fun Set<Field>.findField(name: String, type: String, hasDesc: Boolean = false): Field? {
    val set = if (hasDesc) this.filter { it.description.isNotBlank() } else this
    val field = (set.find { it.name == name && it.type.substringBefore("(") == type }?.copy()
            ?: (set.find { it.name == name && it.type.substringBefore("(").equals(type, true) }?.copy()
                    ?: set.find { it.name.equals(name, true) && it.type.substringBefore("(") == type }?.copy())
            ?: set.find { it.name.equals(name, true) && it.type.substringBefore("(").equals(type, true) }?.copy())
            ?: set.find { it.name == name }?.copy()
            ?: set.find { it.name.equals(name, true) }?.copy()
    return field?.apply { this.name = name }
}