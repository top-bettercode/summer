package top.bettercode.summer.apisign

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.*
import org.springframework.core.type.AnnotatedTypeMetadata
import org.springframework.web.servlet.config.annotation.InterceptorRegistry
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer

/**
 * 接口签名 自动配置

 * @author Peter Wu
 */
@Configuration(proxyBeanMethods = false)
@ConditionalOnWebApplication
@Conditional(ApiSignConfiguration.ApiSignCondition::class)
@EnableConfigurationProperties(ApiSignProperties::class)
class ApiSignConfiguration {

    @Autowired
    lateinit var properties: ApiSignProperties

    /**
     * @return 默认签名算法
     */
    @Bean
    @ConditionalOnMissingBean(ApiSignAlgorithm::class)
    fun apiSignAlgorithm(): ApiSignAlgorithm {
        return ApiSignAlgorithm(properties)
    }

    @Configuration(proxyBeanMethods = false)
    @ConditionalOnWebApplication
    @Conditional(ApiSignCondition::class)
    class ApiSignWebMvcConfigurer : WebMvcConfigurer {
        @Autowired
        lateinit var apiSignAlgorithm: ApiSignAlgorithm

        override fun addInterceptors(registry: InterceptorRegistry) {
            registry.addInterceptor(ApiSignHandlerInterceptor(apiSignAlgorithm))
        }
    }

    internal class ApiSignCondition : Condition {

        override fun matches(context: ConditionContext, metadata: AnnotatedTypeMetadata): Boolean {
            return (!context.environment.getProperty("summer.sign.client-secret").isNullOrBlank()
                    || !context.environment.getProperty("summer.sign.clientSecret").isNullOrBlank()
                    ) &&
                    (!context.environment.getProperty("summer.sign.handler-type-prefix").isNullOrBlank() || !context.environment.getProperty("summer.sign.handlerTypePrefix").isNullOrBlank()
                            )
        }
    }
}