package top.bettercode.summer.web.config

import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import top.bettercode.summer.web.form.FormkeyService
import top.bettercode.summer.web.form.IFormkeyService
import top.bettercode.summer.web.properties.SummerWebProperties
import top.bettercode.summer.web.support.ApplicationContextHolder

@Configuration(proxyBeanMethods = false)
class ApplicationContextHolderConfiguration {
    @Bean
    fun applicationContextHolder(): ApplicationContextHolder {
        return ApplicationContextHolder()
    }


}