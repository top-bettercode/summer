package plugin

import com.fasterxml.jackson.module.kotlin.readValue
import top.bettercode.autodoc.core.Util
import java.io.File
import java.util.*


/**
 *
 * @author Peter Wu
 */
object ConfigTool {

    val filterValues = arrayOf(
        "profiles.active",
        "application.name",
        "admin.server.port",
        "app.server.port",
        "datasource.url",
        "datasource.username",
        "datasource.password"
    )

    fun prettyConfig(configDir: File, ymlFiles: List<File>) {
        val map = linkedMapOf<String, String>()
        ymlFiles.forEach { ymlFile ->
            val lines = ymlFile.readLines()
            var prefix = ""
            lines.filter { it.isNotBlank() }.forEach {
                val key = it.substringBefore(":").trim()
                val value = it.substringAfter(":").trim()
                val itStep = it.substringBefore(":").dropLastWhile { it != ' ' }.length / 2
                prefix = if (prefix.isEmpty()) "" else prefix.split(".").subList(0, itStep)
                    .joinToString(".")
                prefix = if (prefix.isEmpty()) {
                    key
                } else {
                    "$prefix.$key"
                }

                if (value.matches(Regex("@[^@]*@"))) {
                    val va = value.trim('@')
                    if (!filterValues.contains(va) && va != prefix) {
                        map[va] = prefix
                    }
                }
            }
        }
        if (map.isNotEmpty()) {
            ymlFiles.forEach { ymlFile ->
                var text = ymlFile.readText()
                map.forEach { (t, u) ->
                    text = text.replace("@$t@", "@$u@")
                }
                ymlFile.writeText(text)
            }

            configDir.listFiles()?.filter { it.extension == "yml" }?.forEach {
                val properties = toProperties(it)
                var change = false
                map.forEach { (t, u) ->
                    if (properties.containsKey(t)) {
                        properties.setProperty(u, properties.getProperty(t))
                        properties.remove(t)
                        change = true
                    }
                }
                if (change) {
                    properties.store(
                        File(
                            it.parentFile,
                            it.nameWithoutExtension + ".properties"
                        ).outputStream(), ""
                    )
                    it.delete()
                }
            }
        }
    }

    fun toProperties(ymlFile: File): Properties {
        val map = Util.yamlMapper.readValue<Map<String, Any>>(ymlFile)
        val properties = Properties()
        iterateAndProcess(properties, map, "")
        return properties
    }


    private fun iterateAndProcess(
        properties: Properties,
        ymlEntry: Map<String, Any>?,
        rootKey: String
    ) {
        for (key in ymlEntry!!.keys) {
            val value = ymlEntry[key]
            if (value is Map<*, *>) {
                @Suppress("UNCHECKED_CAST")
                iterateAndProcess(
                    properties,
                    value as Map<String, Any>,
                    if (rootKey.isEmpty()) key else "$rootKey.$key"
                )
            } else {
                properties.setProperty(
                    if (rootKey.isEmpty()) key else "$rootKey.$key",
                    value?.toString() ?: ""
                )
            }
        }
    }

}