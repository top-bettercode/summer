package top.bettercode.summer.web.config.summer

import com.fasterxml.jackson.databind.*
import com.fasterxml.jackson.databind.module.SimpleModule
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.support.AbstractBeanDefinition
import org.springframework.boot.autoconfigure.AutoConfigureBefore
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication
import org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration
import org.springframework.boot.autoconfigure.web.servlet.WebMvcRegistrations
import org.springframework.boot.autoconfigure.web.servlet.error.ErrorMvcAutoConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.support.GenericApplicationContext
import org.springframework.core.annotation.AnnotatedElementUtils
import org.springframework.web.servlet.mvc.method.annotation.ExceptionHandlerExceptionResolver
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerAdapter
import top.bettercode.summer.web.*
import top.bettercode.summer.web.error.*
import top.bettercode.summer.web.filter.*
import top.bettercode.summer.web.form.FormkeyService
import top.bettercode.summer.web.form.IFormkeyService
import top.bettercode.summer.web.properties.JacksonExtProperties
import top.bettercode.summer.web.properties.SummerWebProperties
import top.bettercode.summer.web.resolver.ApiExceptionHandlerExceptionResolver
import top.bettercode.summer.web.resolver.ApiRequestMappingHandlerAdapter
import top.bettercode.summer.web.serializer.MixIn
import top.bettercode.summer.web.support.packagescan.PackageScanClassResolver
import java.lang.reflect.ParameterizedType
import java.util.*
import kotlin.collections.HashMap
import kotlin.collections.HashSet

/**
 * Rest MVC 配置
 *
 * @author Peter Wu
 */
@Configuration(proxyBeanMethods = false)
@ConditionalOnWebApplication
@ConditionalOnProperty(prefix = "summer.web", name = ["enable"], havingValue = "true", matchIfMissing = true)
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
