package top.bettercode.summer.tools.feishu

import com.lark.oapi.Client
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

/**
 *
 * @author Peter Wu
 */
@Configuration(proxyBeanMethods = false)
@EnableConfigurationProperties(FeishuProperties::class)
class FeishuConfiguration {


    @Bean
    fun feishuClient(feishuProperties: FeishuProperties): Client {
        return FeishuClientBuilder(feishuProperties).client
    }

}
