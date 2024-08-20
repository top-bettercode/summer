package top.bettercode.summer.env

import org.slf4j.LoggerFactory
import org.springframework.boot.actuate.endpoint.annotation.Endpoint
import org.springframework.boot.actuate.endpoint.annotation.ReadOperation
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
            return loadConfig(manager.environment)?.let {
                val changed = manager.setProperties(it.second)
                if (log.isInfoEnabled) {
                    log.info("load config in:${it.first.uri}/${it.first.configFile}")
                }
                changed
            } ?: emptyMap<String, String>()
        } catch (e: IOException) {
            "配置文件加载错误：\n${e.stackTraceToString()}"
        }
    }
}
