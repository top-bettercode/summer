package top.bettercode.summer.web.config

import org.springframework.boot.autoconfigure.condition.ConditionalOnClass
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import top.bettercode.summer.web.form.FormkeyService
import top.bettercode.summer.web.form.IFormkeyService
import top.bettercode.summer.web.properties.SummerWebProperties

/**
 * @author Peter Wu
 */
@ConditionalOnClass(javax.servlet.http.HttpServletRequest::class)
@Configuration(proxyBeanMethods = false)
class ServletWebConfiguration {

    @ConditionalOnMissingBean(IFormkeyService::class)
    @Bean
    fun formkeyService(summerWebProperties: SummerWebProperties): IFormkeyService {
        return FormkeyService(summerWebProperties.formKeyTtl)
    }

}
