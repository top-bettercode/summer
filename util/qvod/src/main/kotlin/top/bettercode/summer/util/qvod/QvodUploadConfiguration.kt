package top.bettercode.summer.util.qvod

import com.qcloud.vod.VodUploadClient
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

/**
 *
 * @author Peter Wu
 */
@ConditionalOnClass(VodUploadClient::class)
@Configuration(proxyBeanMethods = false)
@EnableConfigurationProperties(QvodProperties::class)
class QvodUploadConfiguration {


    @Bean
    fun qvodUploadClient(qvodProperties: QvodProperties): QvodUploadClient {
        return QvodUploadClient(qvodProperties)
    }


}
