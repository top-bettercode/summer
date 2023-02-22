package top.bettercode.summer.web

import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.context.annotation.Bean
import top.bettercode.summer.tools.weixin.support.IWechatService

/**
 * @author Peter Wu
 */
@SpringBootApplication
class TestApplication {

    companion object {
        @JvmStatic
        fun main(args: Array<String>) {
            SpringApplication.run(TestApplication::class.java, *args)
        }
    }
}