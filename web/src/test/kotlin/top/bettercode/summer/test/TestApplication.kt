package top.bettercode.summer.test

import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.context.annotation.Bean
import top.bettercode.summer.web.resolver.CentConverter
import top.bettercode.summer.web.serializer.CentSerializer.Companion.setNewScale

/**
 * @author Peter Wu
 */
@SpringBootApplication
class TestApplication {

    @Bean
    fun centConverter(): CentConverter {
        return CentConverter()
    }

    companion object {
        @JvmStatic
        fun main(args: Array<String>) {
            SpringApplication.run(TestApplication::class.java, *args)
        }
    }
}