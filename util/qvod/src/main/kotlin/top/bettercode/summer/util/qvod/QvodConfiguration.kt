package top.bettercode.summer.util.qvod

import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

/**
 *
 * @author Peter Wu
 */
@Configuration(proxyBeanMethods = false)
@EnableConfigurationProperties(QvodProperties::class)
class QvodConfiguration {


    @Bean
    fun qvodClient(qvodProperties: QvodProperties): QvodClient {
        return QvodClient(qvodProperties)
    }

    @Bean
    fun qvodController(qvodClient: QvodClient): QvodController {
        return QvodController(qvodClient)
    }

}
