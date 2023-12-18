package top.bettercode.summer.data.jpa.config

import jakarta.persistence.EntityManager
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication
import org.springframework.boot.autoconfigure.data.web.SpringDataWebProperties
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.MessageSource
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.io.ResourceLoader
import org.springframework.data.domain.AuditorAware
import org.springframework.transaction.PlatformTransactionManager
import top.bettercode.summer.data.jpa.support.DataEndpoint
import top.bettercode.summer.data.jpa.support.DataJpaErrorHandler
import top.bettercode.summer.data.jpa.support.DataQuery
import top.bettercode.summer.data.jpa.support.SqlLogAspect
import java.util.*

/**
 * DataJpaConfiguration 配置
 *
 * @author Peter Wu
 */
@Configuration(proxyBeanMethods = false)
@EnableConfigurationProperties(MybatisProperties::class, SpringDataWebProperties::class)
class DataJpaAutoConfiguration {

    @Bean("auditorAware")
    @ConditionalOnMissingBean
    fun auditorAware(): AuditorAware<Any> {
        return AuditorAware<Any> { Optional.empty() }
    }

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

    @Bean
    fun disableSqlLogAspect(): SqlLogAspect {
        return SqlLogAspect()
    }

    @Bean
    fun dataQuery(
        entityManagers: MutableList<EntityManager>,
        transactionManagers: MutableList<PlatformTransactionManager>
    ): DataQuery {
        return DataQuery(entityManagers, transactionManagers)
    }

    @ConditionalOnWebApplication
    @Bean
    fun dataEndpoint(
        @Autowired(required = false) request: HttpServletRequest,
        @Autowired(required = false) response: HttpServletResponse,
        resourceLoader: ResourceLoader,
        dataQuery: DataQuery
    ): DataEndpoint {
        return DataEndpoint(request, response, resourceLoader, dataQuery)
    }

}
