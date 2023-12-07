package top.bettercode.summer.config

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.boot.actuate.autoconfigure.endpoint.web.WebEndpointProperties
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import top.bettercode.summer.tools.lang.util.RandomUtil

/**
 * @author Peter Wu
 */
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
@Configuration(proxyBeanMethods = false)
class ManagementContextConfiguration {

    private val log: Logger = LoggerFactory.getLogger(ManagementContextConfiguration::class.java)

    @ConditionalOnProperty(
            prefix = "summer.management.auth",
            name = ["enabled"],
            havingValue = "true"
    )
    @Bean
    @ConditionalOnMissingBean(ManagementLoginPageGeneratingFilter::class)
    fun managementLoginPageGeneratingFilter(
            managementAuthProperties: ManagementAuthProperties,
            webEndpointProperties: WebEndpointProperties
    ): ManagementLoginPageGeneratingFilter {
        if (managementAuthProperties.password.isNullOrBlank()) {
            managementAuthProperties.password = RandomUtil.nextString2(6)
            log.info(
                    "默认日志访问用户名密码：{}:{}", managementAuthProperties.username,
                    managementAuthProperties.password
            )
        }
        return ManagementLoginPageGeneratingFilter(managementAuthProperties, webEndpointProperties)
    }
}