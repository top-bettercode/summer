package top.bettercode.summer.web.config

import org.springframework.context.ApplicationContext
import org.springframework.context.annotation.Configuration
import org.springframework.core.Ordered
import org.springframework.core.annotation.Order
import top.bettercode.summer.web.support.ApplicationContextHolder

@Order(Ordered.HIGHEST_PRECEDENCE)
@Configuration(proxyBeanMethods = false)
class ApplicationContextHolderConfiguration(applicationContext: ApplicationContext) {

    init {
        ApplicationContextHolder.applicationContext = applicationContext
    }
}