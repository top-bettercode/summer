package top.bettercode.summer.web.config.summer

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.autoconfigure.AutoConfigureBefore
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication
import org.springframework.boot.autoconfigure.web.servlet.error.ErrorMvcAutoConfiguration
import org.springframework.context.MessageSource
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.jdbc.UncategorizedSQLException
import top.bettercode.summer.web.error.DataErrorHandler
import javax.servlet.http.HttpServletRequest

@Configuration(proxyBeanMethods = false)
@ConditionalOnClass(UncategorizedSQLException::class,HttpServletRequest::class)
@AutoConfigureBefore(ErrorMvcAutoConfiguration::class)
@ConditionalOnWebApplication
class DataErrorHandlerConfiguration {

    @Bean
    fun dataErrorHandler(messageSource: MessageSource,
                         @Autowired(required = false) request: HttpServletRequest?): DataErrorHandler {
        return DataErrorHandler(messageSource, request)
    }

}