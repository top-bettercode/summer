package top.bettercode.lang.property

/**
 * @author Peter Wu
 */
interface PropertySource {

    operator fun get(key: String): String?

    fun getOrDefault(key: String, defaultValue: String): String {
        val value = get(key)
        return value ?: defaultValue
    }


    fun put(key: String, value: String?) {
        if (value == null) {
            remove(key)
        } else {
            if (value != get(key)) {
                doPut(key, value)
            }
        }
    }

    fun putIfAbsent(key: String, value: String?) {
        if (value == null) {
            remove(key)
        } else {
            if (null == get(key)) {
                doPut(key, value)
            }
        }
    }

    fun doPut(key: String, value: String)

    fun remove(key: String): String? {
        return if (get(key) != null) {
            doRemove(key)
        } else {
            null;
        }
    }

    fun doRemove(key: String): String?

    fun mapOf(name: String): Map<String, String>

    fun all(): Map<String, String>
}