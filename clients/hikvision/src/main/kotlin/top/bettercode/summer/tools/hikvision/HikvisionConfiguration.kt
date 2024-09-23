package top.bettercode.summer.tools.hikvision

import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

/**
 *
 * @author Peter Wu
 */
@Configuration(proxyBeanMethods = false)
@EnableConfigurationProperties(HikvisionProperties::class)
class HikvisionConfiguration {


    @Bean
    fun jpushClient(hikvisionProperties: HikvisionProperties): HikvisionClient {
        return HikvisionClient(hikvisionProperties)
    }

}
