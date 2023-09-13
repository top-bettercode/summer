package top.bettercode.summer.test

import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.context.annotation.Bean
import top.bettercode.summer.web.resolver.UnitConverter

/**
 * @author Peter Wu
 */
@SpringBootApplication
class TestApplication {

    @Bean
    fun unitConverter(): UnitConverter {
        return UnitConverter()
    }

    companion object {
        @JvmStatic
        fun main(args: Array<String>) {
            SpringApplication.run(TestApplication::class.java, *args)
        }
    }
}