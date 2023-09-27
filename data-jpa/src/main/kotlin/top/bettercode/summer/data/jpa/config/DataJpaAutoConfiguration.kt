package top.bettercode.summer.data.jpa.config

import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.MessageSource
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import top.bettercode.summer.data.jpa.support.DataJpaErrorHandler
import javax.servlet.http.HttpServletRequest

/**
 * DataJpaConfiguration 配置
 *
 * @author Peter Wu
 */
@Configuration(proxyBeanMethods = false)
@EnableConfigurationProperties(MybatisProperties::class)
class DataJpaAutoConfiguration {

    @Bean
    fun datasourcesBeanDefinitionRegistryPostProcessor(): DatasourcesBeanDefinitionRegistryPostProcessor {
        return DatasourcesBeanDefinitionRegistryPostProcessor()
    }

    @Bean("jpaExtProperties")
    @ConditionalOnMissingBean
    fun jpaExtProperties(): JpaExtProperties {
        return JpaExtProperties()
    }

    @ConditionalOnWebApplication
    @Bean
    fun ibatisErrorHandler(
            messageSource: MessageSource,
            request: HttpServletRequest
    ): DataJpaErrorHandler {
        return DataJpaErrorHandler(messageSource, request)
    }
}
