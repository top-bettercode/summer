package top.bettercode.summer.data.jpa

import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.data.jpa.repository.config.EnableJpaAuditing
import org.springframework.transaction.annotation.EnableTransactionManagement
import top.bettercode.summer.data.jpa.config.EnableJpaExtRepositories

@EnableJpaExtRepositories
@SpringBootApplication
@EnableTransactionManagement
@EnableJpaAuditing
class JpaExtApplication {
    companion object {
        @JvmStatic
        fun main(args: Array<String>) {
            SpringApplication.run(JpaExtApplication::class.java, *args)
        }
    }
}