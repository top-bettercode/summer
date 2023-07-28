package top.bettercode.summer.tools.lang.property

import org.springframework.core.io.UrlResource
import java.io.IOException
import java.util.*

/**
 * @author Peter Wu
 */
class PropertiesSource(private vararg val baseName: String) : MapPropertySource(mutableMapOf()) {

    companion object {
        @JvmStatic
        fun of(vararg baseName: String): PropertiesSource {
            return PropertiesSource(*baseName)
        }
    }

    init {
        load()
    }

    fun load() {
        source.clear()
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

    fun load(url: String) {
        source.clear()
        val properties = Properties()
        url.split(",").forEach {
            properties.load(UrlResource(it).inputStream)
        }
        properties.forEach { t, u ->
            source[t.toString()] = u.toString()
        }
    }
}