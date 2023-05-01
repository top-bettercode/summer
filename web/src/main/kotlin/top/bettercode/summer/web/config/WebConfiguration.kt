package top.bettercode.summer.web.config

import org.springframework.boot.autoconfigure.AutoConfigureBefore
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.ApplicationContext
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import top.bettercode.summer.web.form.FormkeyService
import top.bettercode.summer.web.form.IFormkeyService
import top.bettercode.summer.web.properties.JacksonExtProperties
import top.bettercode.summer.web.properties.SummerWebProperties
import top.bettercode.summer.web.support.gb2260.GB2260Controller
import top.bettercode.summer.web.support.packagescan.PackageScanClassResolver

/**
 * @author Peter Wu
 * @since 0.1.14
 */
@AutoConfigureBefore(JacksonAutoConfiguration::class)
@EnableConfigurationProperties(SummerWebProperties::class, JacksonExtProperties::class)
@Configuration(proxyBeanMethods = false)
class WebConfiguration {


    @Bean
    fun packageScanClassResolver(applicationContext: ApplicationContext): PackageScanClassResolver {
        return PackageScanClassResolver(applicationContext.classLoader)
    }

    @Bean
    fun gb2260Controller(): GB2260Controller {
        return GB2260Controller()
    }


}
