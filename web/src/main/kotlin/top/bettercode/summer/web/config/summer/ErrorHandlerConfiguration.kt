package top.bettercode.summer.web.config.summer

import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.boot.autoconfigure.AutoConfigureBefore
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication
import org.springframework.boot.autoconfigure.web.ServerProperties
import org.springframework.boot.autoconfigure.web.servlet.error.ErrorMvcAutoConfiguration
import org.springframework.boot.web.servlet.error.ErrorController
import org.springframework.context.MessageSource
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.MediaType
import org.springframework.web.cors.CorsConfigurationSource
import org.springframework.web.servlet.View
import top.bettercode.summer.web.RespEntity
import top.bettercode.summer.web.error.*
import top.bettercode.summer.web.properties.SummerWebProperties
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

@ConditionalOnClass(HttpServletRequest::class)
@Configuration(proxyBeanMethods = false)
@AutoConfigureBefore(ErrorMvcAutoConfiguration::class)
@ConditionalOnWebApplication
class ErrorHandlerConfiguration {

    @Bean
    fun defaultErrorHandler(messageSource: MessageSource,
                            @Autowired(required = false) request: HttpServletRequest?): DefaultErrorHandler {
        return DefaultErrorHandler(messageSource, request)
    }

    @ConditionalOnMissingBean(ErrorAttributes::class)
    @Bean
    fun errorAttributes(
            @Autowired(required = false) errorHandlers: List<IErrorHandler>?,
            @Autowired(required = false) respEntityConverter: IRespEntityConverter?,
            messageSource: MessageSource,
            serverProperties: ServerProperties,
            summerWebProperties: SummerWebProperties): ErrorAttributes {
        return ErrorAttributes(serverProperties.error, errorHandlers, respEntityConverter,
                messageSource, summerWebProperties)
    }


    @ConditionalOnMissingBean(ErrorController::class)
    @Bean
    fun customErrorController(errorAttributes: ErrorAttributes,
                              serverProperties: ServerProperties,
                              @Autowired(required = false) @Qualifier("corsConfigurationSource") corsConfigurationSource: CorsConfigurationSource?): CustomErrorController {
        return CustomErrorController(errorAttributes, serverProperties.error,
                corsConfigurationSource)
    }


    @Bean(name = ["error"])
    @ConditionalOnMissingBean(name = ["error"])
    fun error(objectMapper: ObjectMapper): View {
        return object : View {
            override fun getContentType(): String {
                return "text/html;charset=utf-8"
            }

            override fun render(model: MutableMap<String, *>?, request: HttpServletRequest, response: HttpServletResponse) {
                if (response.contentType == null) {
                    response.contentType = contentType
                }
                val isPlainText = request.getAttribute(ErrorAttributes.IS_PLAIN_TEXT_ERROR) as Boolean?
                if (isPlainText != null && isPlainText) {
                    response.contentType = MediaType.TEXT_HTML_VALUE
                    response.writer.append(model?.get(RespEntity.KEY_MESSAGE) as String?)
                } else {
                    val result = objectMapper.writeValueAsString(model)
                    response.contentType = MediaType.APPLICATION_JSON_VALUE
                    response.writer.append(result)
                }
            }
        }
    }

}