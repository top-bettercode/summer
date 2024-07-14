package top.bettercode.summer.logging

import org.springframework.boot.autoconfigure.condition.ConditionalOnClass
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import top.bettercode.summer.logging.feign.FeignLogger

/**
 *
 * @author Peter Wu
 */
@ConditionalOnClass(FeignLogger::class)
@Configuration(proxyBeanMethods = false)
class FeignLoggerConfiguration {

    @Bean
    fun feignLogger(): FeignLogger {
        return FeignLogger()
    }
}