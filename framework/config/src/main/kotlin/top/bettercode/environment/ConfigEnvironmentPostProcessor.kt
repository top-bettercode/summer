package top.bettercode.environment

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.boot.env.EnvironmentPostProcessor
import org.springframework.core.env.ConfigurableEnvironment
import org.springframework.boot.SpringApplication
import org.springframework.boot.context.config.ConfigDataEnvironmentPostProcessor
import org.springframework.boot.env.YamlPropertySourceLoader
import org.springframework.core.Ordered
import org.springframework.core.io.ClassPathResource

open class ConfigEnvironmentPostProcessor : EnvironmentPostProcessor, Ordered {

    private val log: Logger = LoggerFactory.getLogger(ConfigEnvironmentPostProcessor::class.java)

    override fun postProcessEnvironment(
        environment: ConfigurableEnvironment, application: SpringApplication
    ) {
        addConfig(environment, "application-core")
        addConfig(environment, "application-default")
    }

    protected fun addConfig(environment: ConfigurableEnvironment, configName: String) {
        val configResource = ClassPathResource("META-INF/${configName}.yml")
        if (configResource.exists()) {
            val configs = YamlPropertySourceLoader().load(
                "META-INF/${configName}.yml",
                configResource
            )
            configs.forEach {
                environment.propertySources.addLast(it)
            }
            if (log.isInfoEnabled) {
                log.info("load config in class path:META-INF/${configName}.yml")
            }
        } else {
            if (log.isInfoEnabled) {
                log.info("No config in class path:META-INF/${configName}.yml")
            }
        }
    }

    override fun getOrder(): Int {
        return ConfigDataEnvironmentPostProcessor.ORDER + 1
    }
}