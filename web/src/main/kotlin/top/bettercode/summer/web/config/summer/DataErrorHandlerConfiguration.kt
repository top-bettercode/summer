package top.bettercode.summer.web.config.summer

import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.boot.autoconfigure.AutoConfigureBefore
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication
import org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration
import org.springframework.boot.autoconfigure.web.ServerProperties
import org.springframework.boot.autoconfigure.web.servlet.error.ErrorMvcAutoConfiguration
import org.springframework.boot.web.servlet.error.ErrorController
import org.springframework.context.MessageSource
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.MediaType
import org.springframework.jdbc.UncategorizedSQLException
import org.springframework.lang.Nullable
import org.springframework.web.cors.CorsConfigurationSource
import org.springframework.web.servlet.View
import top.bettercode.summer.web.RespEntity
import top.bettercode.summer.web.error.*
import top.bettercode.summer.web.properties.SummerWebProperties
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

@ConditionalOnProperty(prefix = "summer.web", name = ["enable"], havingValue = "true", matchIfMissing = true)
@Configuration(proxyBeanMethods = false)
@ConditionalOnClass(UncategorizedSQLException::class)
@AutoConfigureBefore(ErrorMvcAutoConfiguration::class)
@ConditionalOnWebApplication
class DataErrorHandlerConfiguration {

    @Bean
    fun dataErrorHandler(messageSource: MessageSource,
                         @Autowired(required = false) request: HttpServletRequest?): DataErrorHandler {
        return DataErrorHandler(messageSource, request)
    }

}