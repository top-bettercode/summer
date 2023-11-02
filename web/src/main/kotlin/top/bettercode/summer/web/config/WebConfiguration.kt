package top.bettercode.summer.web.config

import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.ApplicationContext
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import top.bettercode.summer.web.properties.JacksonExtProperties
import top.bettercode.summer.web.support.packagescan.PackageScanClassResolver

/**
 * @author Peter Wu
 * @since 0.1.14
 */
@EnableConfigurationProperties(JacksonExtProperties::class)
@Configuration(proxyBeanMethods = false)
class WebConfiguration {

    @Bean
    fun packageScanClassResolver(applicationContext: ApplicationContext): PackageScanClassResolver {
        return PackageScanClassResolver(applicationContext.classLoader)
    }

}
