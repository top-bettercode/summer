package top.bettercode.summer.tools.pay.config

import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Configuration
import top.bettercode.summer.tools.pay.properties.WeixinPayProperties

@EnableConfigurationProperties(WeixinPayProperties::class)
@Configuration(proxyBeanMethods = false)
class WeixinPayConfiguration(private val weixinPayProperties: WeixinPayProperties) {


}