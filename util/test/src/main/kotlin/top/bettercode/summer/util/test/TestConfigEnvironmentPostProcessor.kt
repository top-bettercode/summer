package top.bettercode.summer.util.test

import org.springframework.boot.SpringApplication
import org.springframework.core.Ordered
import org.springframework.core.env.ConfigurableEnvironment
import top.bettercode.config.ConfigEnvironmentPostProcessor

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