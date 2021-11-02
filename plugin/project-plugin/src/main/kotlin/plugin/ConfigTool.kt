package plugin

import com.fasterxml.jackson.core.JsonToken
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory
import com.fasterxml.jackson.dataformat.yaml.YAMLParser
import java.io.File
import java.io.FileInputStream
import java.io.InputStreamReader
import java.nio.charset.Charset
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

    fun prettyConfig(configDir: File,  ymlFiles: List<File>) {
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
                    if (!filterValues.contains(va) && va != prefix && "summer.multipart.file-url-format" != prefix) {
                        map[va] = prefix
                    }
                }
            }
        }
        ymlFiles.forEach { ymlFile ->
            var text = ymlFile.readText()
            map.forEach { (t, u) ->
                text = text.replace("@$t@", "@$u@")
            }
            ymlFile.writeText(text)
        }

        configDir.listFiles()?.forEach {
            val properties = yml2Properties(it)
//            println(StringUtil.valueOf(properties, true))
            map.forEach { (t, u) ->
                if (properties.containsKey(t)) {
                    properties.setProperty(u, properties.getProperty(t))
                    properties.remove(t)
                }
            }
//            System.err.println(StringUtil.valueOf(properties,true))
            properties.store(
                File(
                    it.parentFile,
                    it.nameWithoutExtension + ".properties"
                ).outputStream(), ""
            )
        }

    }

    private const val ENCODING = "utf-8"

    fun yml2Properties(ymlFile: File): Properties {
        val dot = "."
        val properties = Properties()
        val yamlFactory = YAMLFactory()
        val parser: YAMLParser = yamlFactory.createParser(
            InputStreamReader(
                FileInputStream(ymlFile),
                Charset.forName(ENCODING)
            )
        )
        var key = ""
        var value: String?
        var token: JsonToken? = parser.nextToken()
        while (token != null) {
            if (JsonToken.START_OBJECT == token) {
//                key = ""
            } else if (JsonToken.FIELD_NAME == token) {
                if (key.isNotEmpty()) {
                    key += dot
                }
                key += parser.currentName
                token = parser.nextToken()
                if (JsonToken.START_OBJECT == token) {
                    continue
                }
                value = parser.text
                properties[key] = value
                val dotOffset = key.lastIndexOf(dot)
                key = if (dotOffset > 0) {
                    key.substring(0, dotOffset)
                } else {
                    ""
                }
            } else if (JsonToken.END_OBJECT == token) {
                val dotOffset = key.lastIndexOf(dot)
                key = if (dotOffset > 0) {
                    key.substring(0, dotOffset)
                } else {
                    ""
                }
            }
            token = parser.nextToken()
        }
        parser.close()
        return properties
    }
}