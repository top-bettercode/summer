package top.bettercode.summer.util.jpush

import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

/**
 *
 * @author Peter Wu
 */
@Configuration(proxyBeanMethods = false)
@EnableConfigurationProperties(JpushProperties::class)
class JpushConfiguration {


    @Bean
    fun jpushClient(jpushProperties: JpushProperties): JpushClient {
        return JpushClient(jpushProperties)
    }

}
