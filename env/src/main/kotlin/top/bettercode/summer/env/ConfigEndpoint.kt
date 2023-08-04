package top.bettercode.summer.env

import org.springframework.boot.actuate.endpoint.annotation.Endpoint
import org.springframework.boot.actuate.endpoint.annotation.ReadOperation
import org.springframework.boot.env.YamlPropertySourceLoader
import org.springframework.core.env.MapPropertySource
import org.springframework.core.io.UrlResource
import java.io.IOException
import java.io.PrintWriter
import java.io.StringWriter

/**
 * @author Peter Wu
 */
@Endpoint(id = "config")
class ConfigEndpoint(private val environment: EnvironmentManager) {
    @ReadOperation
    fun write(ymlUrl: String): Any {
        return try {
            val changed: MutableMap<String, String?> = HashMap()
            val propertySources = YamlPropertySourceLoader().load("remote",
                    UrlResource(ymlUrl))
            for (propertySource in propertySources) {
                val mapPropertySource = propertySource as MapPropertySource
                for ((key, value) in mapPropertySource.source) {
                    val `val` = value?.toString()
                    val change = environment.setProperty(key, `val`)
                    if (change) {
                        changed[key] = `val`
                    }
                }
            }
            changed
        } catch (e: IOException) {
            val stringWriter = StringWriter()
            val printWriter = PrintWriter(stringWriter)
            e.printStackTrace(printWriter)
            printWriter.flush()
            printWriter.close()
            "配置文件加载错误：\n$stringWriter"
        }
    }
}
