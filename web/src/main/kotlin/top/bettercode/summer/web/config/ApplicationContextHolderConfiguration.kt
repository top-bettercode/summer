package top.bettercode.summer.web.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import top.bettercode.summer.web.support.ApplicationContextHolder

@Configuration(proxyBeanMethods = false)
class ApplicationContextHolderConfiguration {
    @Bean
    fun applicationContextHolder(): ApplicationContextHolder {
        return ApplicationContextHolder()
    }

}