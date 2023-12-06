package top.bettercode.summer.data.jpa.config

import org.springframework.boot.autoconfigure.AutoConfigureBefore
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication
import org.springframework.boot.autoconfigure.data.web.SpringDataWebAutoConfiguration
import org.springframework.boot.autoconfigure.data.web.SpringDataWebProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.data.domain.Pageable
import org.springframework.data.web.PageableHandlerMethodArgumentResolver
import org.springframework.data.web.config.PageableHandlerMethodArgumentResolverCustomizer
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer

@Configuration(proxyBeanMethods = false)
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
@ConditionalOnClass(PageableHandlerMethodArgumentResolver::class, WebMvcConfigurer::class)
@ConditionalOnMissingBean(PageableHandlerMethodArgumentResolver::class)
@AutoConfigureBefore(SpringDataWebAutoConfiguration::class)
class DataWebAutoConfiguration(private val properties: SpringDataWebProperties) {
    @Bean
    @ConditionalOnMissingBean
    fun pageableCustomizer(): PageableHandlerMethodArgumentResolverCustomizer {
        return PageableHandlerMethodArgumentResolverCustomizer { resolver: PageableHandlerMethodArgumentResolver ->
            val pageable = properties.pageable
            resolver.setPageParameterName(pageable.pageParameter)
            resolver.setSizeParameterName(pageable.sizeParameter)
            resolver.setOneIndexedParameters(pageable.isOneIndexedParameters)
            resolver.setPrefix(pageable.prefix)
            resolver.setQualifierDelimiter(pageable.qualifierDelimiter)
            resolver.setFallbackPageable(Pageable.unpaged())
            resolver.setMaxPageSize(pageable.maxPageSize)
        }
    }
}