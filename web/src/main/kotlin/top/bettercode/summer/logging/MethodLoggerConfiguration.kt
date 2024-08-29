package top.bettercode.summer.logging

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import top.bettercode.summer.logging.async.MethodLoggingAspect

/**
 *
 * @author Peter Wu
 */
@Configuration(proxyBeanMethods = false)
class MethodLoggerConfiguration {

    @Bean
    fun methodLoggingAspect(): MethodLoggingAspect {
        return MethodLoggingAspect()
    }
}