package top.bettercode.summer.web.config.summer

import org.springframework.boot.autoconfigure.condition.ConditionalOnClass
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication
import org.springframework.boot.autoconfigure.web.servlet.WebMvcRegistrations
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.servlet.mvc.method.annotation.ExceptionHandlerExceptionResolver
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerAdapter
import top.bettercode.summer.web.error.ErrorAttributes
import top.bettercode.summer.web.filter.*
import top.bettercode.summer.web.form.FormkeyService
import top.bettercode.summer.web.form.IFormkeyService
import top.bettercode.summer.web.properties.SummerWebProperties
import top.bettercode.summer.web.resolver.ApiExceptionHandlerExceptionResolver
import top.bettercode.summer.web.resolver.ApiRequestMappingHandlerAdapter

/**
 * Rest MVC 配置
 *
 * @author Peter Wu
 */
@ConditionalOnClass(javax.servlet.Filter::class)
@Configuration(proxyBeanMethods = false)
@ConditionalOnWebApplication
class WebMvcConfiguration {

    @ConditionalOnMissingBean(IFormkeyService::class)
    @Bean
    fun formkeyService(summerWebProperties: SummerWebProperties): IFormkeyService {
        return FormkeyService(summerWebProperties.formExpireSeconds)
    }

    @ConditionalOnMissingBean(IApiVersionService::class)
    @Bean
    fun apiVersionService(summerWebProperties: SummerWebProperties): IApiVersionService {
        return DefaultApiVersionService(summerWebProperties)
    }

    /*
     * 响应增加api version
     */
    @Bean
    fun apiVersionFilter(apiVersionService: IApiVersionService): ApiVersionFilter {
        return ApiVersionFilter(apiVersionService)
    }

    /*
     * 隐藏方法，网页支持
     */
    @Bean
    fun hiddenHttpMethodFilter(): OrderedHiddenHttpMethodFilter {
        return OrderedHiddenHttpMethodFilter()
    }

    /*
     * Put方法，网页支持
     */
    @Bean
    fun putFormContentFilter(): OrderedHttpPutFormContentFilter {
        return OrderedHttpPutFormContentFilter()
    }

    @ConditionalOnProperty(prefix = "summer.web", name = ["enabled"], havingValue = "true", matchIfMissing = true)
    @Bean
    fun webMvcRegistrations(summerWebProperties: SummerWebProperties, errorAttributes: ErrorAttributes): WebMvcRegistrations {
        return object : WebMvcRegistrations {
            override fun getRequestMappingHandlerAdapter(): RequestMappingHandlerAdapter {
                return ApiRequestMappingHandlerAdapter(summerWebProperties, errorAttributes)
            }

            override fun getExceptionHandlerExceptionResolver(): ExceptionHandlerExceptionResolver {
                return ApiExceptionHandlerExceptionResolver(summerWebProperties, errorAttributes)
            }
        }
    }

}
