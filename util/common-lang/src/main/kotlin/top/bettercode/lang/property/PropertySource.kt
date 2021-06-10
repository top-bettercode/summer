package top.bettercode.lang.property

/**
 * @author Peter Wu
 */
interface PropertySource {

    operator fun get(key: String): Any?

    fun getString(key: String): String? {
        val v = get(key)
        return v?.toString()
    }

    fun getOrDefault(key: String, defaultValue: Any): Any {
        val value = get(key)
        return value ?: defaultValue
    }

    fun getOrDefault(key: String, defaultValue: String): String {
        val value = getString(key)
        return value ?: defaultValue
    }

    fun put(key: String, value: Any?) {
        if (value == null) {
            remove(key)
        } else {
            if (value != get(key)) {
                doPut(key, value)
            }
        }
    }

    fun doPut(key: String, value: Any)

    fun remove(key: String) {
        if (get(key) != null) {
            doRemove(key)
        }
    }

    fun doRemove(key: String)

    fun mapOf(name: String): Map<String, Any>

    fun all(): Map<Any, Any>
}