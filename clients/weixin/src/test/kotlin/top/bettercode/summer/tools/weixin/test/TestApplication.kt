package top.bettercode.summer.tools.weixin.test

import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.context.annotation.Bean
import top.bettercode.summer.tools.weixin.support.IWeixinService

/**
 * @author Peter Wu
 */
@SpringBootApplication
class TestApplication {

    @Bean
    fun wechatService(): IWeixinService {
        return object : IWeixinService {
        }
    }

    companion object {
        @JvmStatic
        fun main(args: Array<String>) {
            SpringApplication.run(TestApplication::class.java, *args)
        }
    }
}