package top.bettercode.summer.web.config

import org.springframework.boot.autoconfigure.condition.ConditionalOnClass
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import top.bettercode.summer.web.properties.SummerWebProperties
import top.bettercode.summer.web.support.gb2260.GB2260Controller
import javax.servlet.http.HttpServletRequest

/**
 * @author Peter Wu
 * @since 0.1.14
 */
@ConditionalOnClass(HttpServletRequest::class)
@EnableConfigurationProperties(SummerWebProperties::class)
@Configuration(proxyBeanMethods = false)
@ConditionalOnWebApplication
class ControllerConfiguration {


    @Bean
    fun gb2260Controller(): GB2260Controller {
        return GB2260Controller()
    }

}
