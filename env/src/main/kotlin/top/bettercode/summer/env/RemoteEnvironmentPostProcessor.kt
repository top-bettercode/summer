package top.bettercode.summer.env

import org.eclipse.jgit.api.Git
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.boot.SpringApplication
import org.springframework.boot.context.config.ConfigFileApplicationListener
import org.springframework.boot.context.properties.bind.Binder
import org.springframework.boot.env.EnvironmentPostProcessor
import org.springframework.boot.env.YamlPropertySourceLoader
import org.springframework.core.Ordered
import org.springframework.core.env.ConfigurableEnvironment
import org.springframework.core.env.PropertySource
import org.springframework.core.env.get
import org.springframework.core.io.FileUrlResource
import top.bettercode.summer.tools.lang.util.FileUtil
import java.io.File

open class RemoteEnvironmentPostProcessor : EnvironmentPostProcessor, Ordered {

    private val log: Logger = LoggerFactory.getLogger(RemoteEnvironmentPostProcessor::class.java)

    override fun postProcessEnvironment(
        environment: ConfigurableEnvironment, application: SpringApplication
    ) {
        loadConfig(environment)?.let {
            it.second.forEach { propertySource ->
                environment.propertySources.addFirst(propertySource)
            }
            if (log.isInfoEnabled) {
                log.info("load config in:${it.first.uri}/${it.first.configFile}")
            }
        }
    }

    override fun getOrder(): Int {
        return ConfigFileApplicationListener.DEFAULT_ORDER + 2
    }

    companion object {
        private val log = LoggerFactory.getLogger(RemoteEnvironmentPostProcessor::class.java)

        fun loadConfig(environment: ConfigurableEnvironment): Pair<ConfigProperties, List<PropertySource<*>>>? {
            val config = Binder.get(environment).bind(
                "summer.config.server.git", ConfigProperties::class.java
            ).orElse(null)
            return if (config != null && !config.uri.isNullOrBlank() && !config.configFile.isNullOrBlank()) {
                val configPath = config.configFile!!
                val url = config.uri!!
                val configUrl = clone(
                    url,
                    environment["spring.application.name"]!!,
                    config.username!!,
                    config.password!!
                ).resolve(configPath).toURI().toURL()
                val propertySources = YamlPropertySourceLoader().load(
                    "remote",
                    FileUrlResource(configUrl)
                )
                config to propertySources
            } else
                null
        }

        fun clone(url: String, appName: String, username: String, password: String): File {
            val dir = File(FileUtil.tmpDir, "config${File.separator}$appName")
            if (dir.exists()) {
                dir.deleteRecursively()
                if (log.isDebugEnabled) {
                    log.debug("exist:$dir deleted")
                }
            }
            Git.cloneRepository()
                .setURI(url)
                .setDirectory(dir)
                .setCredentialsProvider(UsernamePasswordCredentialsProvider(username, password))
                .call().use {
                    if (log.isDebugEnabled) {
                        log.debug("repository:$url cloned successfully")
                    }
                    return dir
                }
        }
    }
}