package top.bettercode.summer.tools.hikvision

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

/**
 *
 * @author Peter Wu
 */
@ConditionalOnProperty(prefix = "summer.hikvision", name = ["host"])
@Configuration(proxyBeanMethods = false)
@EnableConfigurationProperties(HikvisionProperties::class)
class HikvisionConfiguration {


    @Bean
    fun hikvisionClient(hikvisionProperties: HikvisionProperties): HikvisionClient {
        return HikvisionClient(hikvisionProperties)
    }

}
