package top.bettercode.summer.web.config

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.ApplicationContext
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.EnableAspectJAutoProxy
import top.bettercode.summer.tools.lang.util.TimeUtil
import top.bettercode.summer.tools.lang.util.WatchdogUtil
import top.bettercode.summer.web.properties.JacksonExtProperties
import top.bettercode.summer.web.properties.SummerWebProperties
import top.bettercode.summer.web.support.packagescan.PackageScanClassResolver

/**
 * @author Peter Wu
 */
@EnableConfigurationProperties(JacksonExtProperties::class, SummerWebProperties::class)
@EnableAspectJAutoProxy(proxyTargetClass = true)
@Configuration(proxyBeanMethods = false)
class WebConfiguration {
    private val log: Logger = LoggerFactory.getLogger(WebConfiguration::class.java)

    init {
        try {
            TimeUtil.checkTime()
            WatchdogUtil.schedule {
                TimeUtil.checkTime()
            }
        } catch (e: Exception) {
            log.error("check time error", e)
        }
    }

    @Bean
    fun packageScanClassResolver(applicationContext: ApplicationContext): PackageScanClassResolver {
        return PackageScanClassResolver(applicationContext.classLoader)
    }

}
