package top.bettercode.summer.tools.amap

import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

/**
 * @author Peter Wu
 */
@EnableConfigurationProperties(AMapProperties::class)
@Configuration(proxyBeanMethods = false)
class AMapConfiguration {

    @Bean
    fun aMapClient(amapProperties: AMapProperties): AMapClient {
        return AMapClient(amapProperties)
    }

}
