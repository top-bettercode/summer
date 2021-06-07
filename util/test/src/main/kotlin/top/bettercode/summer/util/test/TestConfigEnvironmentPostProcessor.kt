package top.bettercode.summer.util.test

import top.bettercode.config.ConfigEnvironmentPostProcessor
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.boot.env.EnvironmentPostProcessor
import org.springframework.core.env.ConfigurableEnvironment
import org.springframework.boot.SpringApplication
import org.springframework.boot.env.YamlPropertySourceLoader
import org.springframework.core.Ordered
import org.springframework.core.io.ClassPathResource

class TestConfigEnvironmentPostProcessor : ConfigEnvironmentPostProcessor(), Ordered {

    override fun postProcessEnvironment(
        environment: ConfigurableEnvironment, application: SpringApplication
    ) {
        addConfig(environment, "application-test")
        addConfig(environment, "application-test-default")
    }

    override fun getOrder(): Int {
        return Ordered.HIGHEST_PRECEDENCE
    }
}