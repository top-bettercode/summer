package top.bettercode.summer.web.config.summer

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication
import org.springframework.boot.autoconfigure.web.servlet.WebMvcRegistrations
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.servlet.mvc.method.annotation.ExceptionHandlerExceptionResolver
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerAdapter
import top.bettercode.summer.web.error.ErrorAttributes
import top.bettercode.summer.web.error.IRespEntityConverter
import top.bettercode.summer.web.filter.*
import top.bettercode.summer.web.properties.SummerWebProperties
import top.bettercode.summer.web.resolver.ApiExceptionHandlerExceptionResolver
import top.bettercode.summer.web.resolver.ApiRequestMappingHandlerAdapter

/**
 * Rest MVC 配置
 *
 * @author Peter Wu
 */
@Configuration(proxyBeanMethods = false)
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
class WebMvcConfiguration {


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

    @Bean
    fun webMvcRegistrations(summerWebProperties: SummerWebProperties, errorAttributes: ErrorAttributes, @Autowired(required = false) respEntityConverter: IRespEntityConverter?): WebMvcRegistrations {
        return object : WebMvcRegistrations {
            override fun getRequestMappingHandlerAdapter(): RequestMappingHandlerAdapter {
                return ApiRequestMappingHandlerAdapter(summerWebProperties, errorAttributes, respEntityConverter)
            }

            override fun getExceptionHandlerExceptionResolver(): ExceptionHandlerExceptionResolver {
                return ApiExceptionHandlerExceptionResolver(summerWebProperties, errorAttributes, respEntityConverter)
            }
        }
    }

}
