package top.bettercode.summer.util.mobile

import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

/**
 *
 * @author Peter Wu
 */
@Configuration(proxyBeanMethods = false)
@EnableConfigurationProperties(MobileQueryProperties::class)
class MobileQueryConfiguration {


    @Bean
    fun mobileQueryClient(mobileQueryProperties: MobileQueryProperties): IMobileQueryClient {
        return MobileQueryClient(mobileQueryProperties)
    }

}
