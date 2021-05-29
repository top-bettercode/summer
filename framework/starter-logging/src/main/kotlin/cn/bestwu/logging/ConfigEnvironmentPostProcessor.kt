package cn.bestwu.logging

import org.springframework.boot.env.EnvironmentPostProcessor
import org.springframework.core.env.ConfigurableEnvironment
import org.springframework.boot.SpringApplication
import org.springframework.boot.env.YamlPropertySourceLoader
import org.springframework.core.Ordered
import org.springframework.core.io.ClassPathResource

class ConfigEnvironmentPostProcessor : EnvironmentPostProcessor, Ordered {
    override fun postProcessEnvironment(
        environment: ConfigurableEnvironment,
        application: SpringApplication
    ) {
        val configs = YamlPropertySourceLoader().load(
            "META-INF/bootstrap-base.yml",
            ClassPathResource("META-INF/bootstrap-base.yml")
        )
        configs.forEach {
            environment.propertySources.addFirst(it)
        }
    }

    override fun getOrder(): Int {
        return Ordered.HIGHEST_PRECEDENCE
    }
}