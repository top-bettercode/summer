package top.bettercode.summer.web.config

import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import top.bettercode.summer.web.support.division.DivisionDataController

/**
 * @author Peter Wu
 * @since 0.1.14
 */
@Configuration(proxyBeanMethods = false)
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
class ControllerConfiguration {


    @Bean
    fun gb2260Controller(): DivisionDataController {
        return DivisionDataController()
    }

}
