package top.bettercode.summer.tools.lang.property

import org.springframework.core.convert.ConversionService
import org.springframework.core.convert.support.DefaultConversionService
import java.io.Serializable

/**
 * @author Peter Wu
 */
open class MapPropertySource(val source: MutableMap<String, String>) : PropertySource {

    private val conversionService: ConversionService = DefaultConversionService()

    override fun get(key: String): String? {
        return source[key]
    }

    override fun doPut(key: String, value: String) {
        source[key] = value
    }

    override fun doRemove(key: String): String? {
        return source.remove(key)
    }

    override fun mapOf(name: String): Map<String, String> {
        val map: MutableMap<String, String> = HashMap()
        source.forEach { (k: String, v: String) ->
            val prefix = "$name."
            if (k.startsWith(prefix)) {
                map[k.substring(prefix.length)] = v
            }
        }
        return map
    }

    override fun mapOf(name: String, javaTypeName: String?): Map<Serializable, String> {
        var typeName = javaTypeName ?: "java.lang.String"
        if (typeName == "Int") {
            typeName = "int"
        } else if ("String" == typeName) {
            typeName = "java.lang.String"
        }
        val type: Class<*> = when (typeName) {
            "boolean" -> Boolean::class.java
            "byte" -> Byte::class.java
            "short" -> Short::class.java
            "int" -> Int::class.java
            "long" -> Long::class.java
            "float" -> Float::class.java
            "double" -> Double::class.java
            else -> Class.forName(typeName)
        }

        val map: MutableMap<Serializable, String> = HashMap()
        source.forEach { (k: String, v: String) ->
            val prefix = "$name."
            if (k.startsWith(prefix)) {
                val key = k.substring(prefix.length)
                if (conversionService.canConvert(String::class.java, type)) {
                    val newKey = conversionService.convert(key, type) as Serializable
                    map[newKey] = v
                } else
                    map[key] = v
            }
        }
        return map
    }

    override fun all(): Map<String, String> {
        return source
    }

    override fun clear() {
        source.clear()
    }
}