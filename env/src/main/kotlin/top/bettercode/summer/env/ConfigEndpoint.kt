package top.bettercode.summer.env

import org.slf4j.LoggerFactory
import org.springframework.boot.actuate.endpoint.annotation.Endpoint
import org.springframework.boot.actuate.endpoint.annotation.ReadOperation
import org.springframework.core.env.MapPropertySource
import top.bettercode.summer.env.RemoteEnvironmentPostProcessor.Companion.loadConfig
import java.io.IOException

/**
 * @author Peter Wu
 */
@Endpoint(id = "config")
class ConfigEndpoint(private val manager: EnvironmentManager) {

    private val log = LoggerFactory.getLogger(ConfigEndpoint::class.java)

    @ReadOperation
    fun write(): Any {
        return try {
            val changed: MutableMap<String, String?> = HashMap()
            loadConfig(manager.environment)?.let {
                it.second.forEach { propertySource ->
                    val mapPropertySource = propertySource as MapPropertySource
                    for ((key, value) in mapPropertySource.source) {
                        val `val` = value?.toString()
                        val change = manager.setProperty(key, `val`)
                        if (change) {
                            changed[key] = `val`
                        }
                    }
                }
                if (log.isInfoEnabled) {
                    log.info("load config in:${it.first.uri}/${it.first.configFile}")
                }
            }
            changed
        } catch (e: IOException) {
            "配置文件加载错误：\n${e.stackTraceToString()}"
        }
    }
}
