package cn.bestwu.lang.property

import java.util.*

/**
 * @author Peter Wu
 */
open class MapPropertySource(protected val source: MutableMap<Any, Any>) : PropertySource {

    override fun get(key: String): Any? {
        return source[key]
    }

    override fun doPut(key: String, value: Any) {
        source[key] = value
    }

    override fun doRemove(key: String) {
        source.remove(key)
    }

    override fun mapOf(name: String): Map<Any, Any> {
        val map: MutableMap<Any, Any> = HashMap()
        source.forEach { (k: Any, v: Any) ->
            val key = k as String
            if (key == name) {
                map[""] = v
            } else {
                val prefix = "$name."
                if (key.startsWith(prefix)) {
                    map[key.substring(prefix.length)] = v
                }
            }
        }
        return map
    }

    override fun all(): Map<Any, Any> {
        return source
    }
}