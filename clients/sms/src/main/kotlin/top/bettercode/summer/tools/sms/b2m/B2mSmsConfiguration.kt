package top.bettercode.summer.tools.sms.b2m

import org.slf4j.LoggerFactory
import org.springframework.boot.autoconfigure.AutoConfigureAfter
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import top.bettercode.summer.config.EndpointAutoConfiguration
import top.bettercode.summer.tools.lang.util.WatchdogUtil

/**
 * 亿美软通短信平台 配置
 *
 * @author Peter Wu
 */
@ConditionalOnProperty(prefix = "summer.sms.b2m", value = ["app-id"])
@Configuration(proxyBeanMethods = false)
@AutoConfigureAfter(EndpointAutoConfiguration::class)
@EnableConfigurationProperties(B2mSmsProperties::class)
class B2mSmsConfiguration {

    private val log = LoggerFactory.getLogger(B2mSmsConfiguration::class.java)

    @Bean
    fun simpleB2mSmsTemplate(b2mProperties: B2mSmsProperties): SimpleB2mSmsTemplate {
        return SimpleB2mSmsTemplate(b2mProperties)
    }

    @Bean
    fun b2mTemplate(b2mProperties: B2mSmsProperties): B2mSmsTemplate {
        val b2mSmsTemplate = B2mSmsTemplate(b2mProperties)
        try {
            if (b2mProperties.checkBalance) {
                b2mSmsTemplate.checkBalance()
                WatchdogUtil.schedule {
                    b2mSmsTemplate.checkBalance()
                }
            }
        } catch (e: Exception) {
            log.error("check b2m sms balance error", e)
        }
        return b2mSmsTemplate
    }
}