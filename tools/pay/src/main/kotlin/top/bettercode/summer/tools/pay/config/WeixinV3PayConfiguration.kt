package top.bettercode.summer.tools.pay.config

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import top.bettercode.summer.tools.pay.properties.WeixinV3PayProperties
import top.bettercode.summer.tools.pay.weixinv3.WeixinV3PayClient

@ConditionalOnProperty(prefix = "summer.pay.weixin-v3", name = ["merchant-id"])
@EnableConfigurationProperties(WeixinV3PayProperties::class)
@Configuration(proxyBeanMethods = false)
class WeixinV3PayConfiguration(private val weixinPayProperties: WeixinV3PayProperties) {

    @Bean
    fun weixinV3PayClient(): WeixinV3PayClient {
        return WeixinV3PayClient(weixinPayProperties)
    }


}