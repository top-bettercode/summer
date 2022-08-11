package top.bettercode.summer.util.weather

import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

/**
 *
 * @author Peter Wu
 */
@Configuration(proxyBeanMethods = false)
@EnableConfigurationProperties(WeatherProperties::class)
class WeatherConfiguration {


    @Bean
    fun weatherClient(weatherProperties: WeatherProperties): WeatherClient {
        return WeatherClient(weatherProperties)
    }

}
