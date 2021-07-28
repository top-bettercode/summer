package top.bettercode.lang.property

import java.util.*

/**
 * @author Peter Wu
 */
open class MapPropertySource(protected val source: MutableMap<String, String>) : PropertySource {

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

    override fun all(): Map<String, String> {
        return source
    }

    override fun clear() {
        source.clear()
    }
}