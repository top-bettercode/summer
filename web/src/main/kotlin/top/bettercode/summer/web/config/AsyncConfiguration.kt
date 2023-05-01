package top.bettercode.summer.web.config

import org.slf4j.LoggerFactory
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.context.annotation.Configuration
import org.springframework.scheduling.annotation.EnableAsync

/**
 * @author Peter Wu
 */
@ConditionalOnProperty(name = ["summer.async.enabled"], havingValue = "true")
@EnableAsync(proxyTargetClass = true)
@Configuration(proxyBeanMethods = false)
class AsyncConfiguration {
    init {
        val log = LoggerFactory.getLogger(AsyncConfiguration::class.java)
        log.info("------------启用 Spring 的异步方法执行功能------------")
    }
}
