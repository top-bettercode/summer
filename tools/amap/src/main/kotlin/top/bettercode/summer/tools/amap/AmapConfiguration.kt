package top.bettercode.summer.tools.amap

import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

/**
 * @author Peter Wu
 */
@EnableConfigurationProperties(AmapProperties::class)
@Configuration(proxyBeanMethods = false)
class AmapConfiguration {

    @Bean
    fun aMapClient(amapProperties: AmapProperties): AMapClient {
        return AMapClient(amapProperties)
    }

}
