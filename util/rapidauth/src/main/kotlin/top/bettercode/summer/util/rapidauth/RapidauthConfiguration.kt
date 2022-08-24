package top.bettercode.summer.util.rapidauth

import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

/**
 *
 * @author Peter Wu
 */
@Configuration(proxyBeanMethods = false)
@EnableConfigurationProperties(RapidauthProperties::class)
class RapidauthConfiguration {


    @Bean
    fun rapidauthClient(rapidauthProperties: RapidauthProperties): IRapidauthClient {
        return RapidauthClient(rapidauthProperties)
    }

}
