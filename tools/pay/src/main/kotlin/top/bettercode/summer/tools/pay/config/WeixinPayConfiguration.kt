package top.bettercode.summer.tools.pay.config

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import top.bettercode.summer.tools.pay.properties.WeixinPayProperties
import top.bettercode.summer.tools.pay.weixin.WeixinPayClient

@ConditionalOnProperty(prefix = "summer.pay.weixin", name = ["mch-id"])
@EnableConfigurationProperties(WeixinPayProperties::class)
@Configuration(proxyBeanMethods = false)
class WeixinPayConfiguration(private val weixinPayProperties: WeixinPayProperties) {

    @Bean
    fun weixinPayClient(): WeixinPayClient {
        return WeixinPayClient(weixinPayProperties)
    }

}