package top.bettercode.lang.property

import java.io.IOException
import java.util.*

/**
 * @author Peter Wu
 */
class PropertiesSource(vararg baseName: String) : MapPropertySource(mutableMapOf()) {
    companion object {
        @JvmStatic
        fun of(vararg baseName: String): PropertiesSource {
            return PropertiesSource(*baseName)
        }
    }

    /**
     * @param baseName Bundle baseName
     */
    init {
        val properties = Properties()
        for (name in baseName) {
            try {
                val resourceAsStream = PropertiesSource::class.java
                    .getResourceAsStream("/$name.properties")
                if (resourceAsStream != null) {
                    properties.load(resourceAsStream)
                }
            } catch (ignored: IOException) {
            }
        }
        properties.forEach { t, u ->
            source[t.toString()] = u.toString()
        }
    }
}