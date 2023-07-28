package top.bettercode.summer.tools.sms.aliyun

import org.slf4j.LoggerFactory
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

/**
 * 阿里短信平台 配置
 *
 * @author Peter Wu
 */
@ConditionalOnProperty(prefix = "summer.sms.aliyun", value = ["access-key-id"])
@Configuration(proxyBeanMethods = false)
@EnableConfigurationProperties(AliSmsProperties::class)
class AliSmsConfiguration {
    private val log = LoggerFactory.getLogger(AliSmsConfiguration::class.java)

    @Bean
    fun aliSmsTemplate(aliSmsProperties: AliSmsProperties?): AliSmsTemplate {
        return AliSmsTemplate(aliSmsProperties!!)
    }
}