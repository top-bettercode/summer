package top.bettercode.summer.web.config

import org.slf4j.LoggerFactory
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.context.annotation.Configuration
import org.springframework.scheduling.annotation.EnableScheduling

/**
 * @author Peter Wu
 */
@ConditionalOnProperty(value = ["summer.scheduling.enabled"], havingValue = "true")
@EnableScheduling
@Configuration(proxyBeanMethods = false)
class SchedulingConfiguration {
    init {
        val log = LoggerFactory.getLogger(SchedulingConfiguration::class.java)
        log.info("------------启用 Spring 的计划任务执行功能------------")
    }
}
