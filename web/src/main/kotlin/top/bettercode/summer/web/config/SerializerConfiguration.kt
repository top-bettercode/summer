package top.bettercode.summer.web.config

import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.Ordered
import org.springframework.core.annotation.Order
import org.springframework.core.env.Environment
import top.bettercode.summer.web.properties.JacksonExtProperties
import top.bettercode.summer.web.serializer.CustomNullSerializerModifier
import top.bettercode.summer.web.serializer.UrlSerializer
import top.bettercode.summer.web.support.code.CodeServiceHolder
import top.bettercode.summer.web.support.code.ICodeService

@Configuration(proxyBeanMethods = false)
@Order(Ordered.HIGHEST_PRECEDENCE)
class SerializerConfiguration(environment: Environment,
                              objectMapper: ObjectMapper,
                              jacksonExtProperties: JacksonExtProperties) {
    init {
        UrlSerializer.setEnvironment(environment)
        objectMapper.setSerializerFactory(objectMapper.serializerFactory
                .withSerializerModifier(
                        CustomNullSerializerModifier(jacksonExtProperties)))
    }

    @ConditionalOnMissingBean(name = [CodeServiceHolder.DEFAULT_BEAN_NAME])
    @Bean(CodeServiceHolder.DEFAULT_BEAN_NAME)
    fun defaultCodeService(): ICodeService {
        return CodeServiceHolder.PROPERTIES_CODESERVICE
    }
}