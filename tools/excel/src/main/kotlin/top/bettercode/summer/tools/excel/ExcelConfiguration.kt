package top.bettercode.summer.tools.excel

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication
import org.springframework.context.MessageSource
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import top.bettercode.summer.web.error.IErrorHandler
import javax.servlet.http.HttpServletRequest

@ConditionalOnClass(IErrorHandler::class)
@Configuration(proxyBeanMethods = false)
@ConditionalOnWebApplication
class ExcelConfiguration {
    @Bean
    fun excelErrorHandler(messageSource: MessageSource?,
                          @Autowired(required = false) request: HttpServletRequest?): ExcelErrorHandler {
        return ExcelErrorHandler(messageSource, request)
    }
}