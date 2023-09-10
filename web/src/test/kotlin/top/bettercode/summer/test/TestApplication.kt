package top.bettercode.summer.test

import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.context.annotation.Bean
import top.bettercode.summer.web.resolver.UnitGenericConverter

/**
 * @author Peter Wu
 */
@SpringBootApplication
class TestApplication {

    @Bean
    fun unitGenericConverter(): UnitGenericConverter {
        return UnitGenericConverter()
    }

    companion object {
        @JvmStatic
        fun main(args: Array<String>) {
            SpringApplication.run(TestApplication::class.java, *args)
        }
    }
}