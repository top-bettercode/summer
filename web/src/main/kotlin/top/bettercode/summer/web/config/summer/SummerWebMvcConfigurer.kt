package top.bettercode.summer.web.config.summer

import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication
import org.springframework.context.MessageSource
import org.springframework.context.annotation.Configuration
import org.springframework.core.Ordered
import org.springframework.format.FormatterRegistry
import org.springframework.web.servlet.config.annotation.InterceptorRegistry
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer
import top.bettercode.summer.tools.lang.util.TimeUtil.Companion.of
import top.bettercode.summer.web.deprecated.DeprecatedAPIInterceptor
import top.bettercode.summer.web.form.FormDuplicateCheckInterceptor
import top.bettercode.summer.web.form.IFormkeyService
import top.bettercode.summer.web.properties.SummerWebProperties
import java.time.LocalDate
import java.time.LocalDateTime

@Configuration(proxyBeanMethods = false)
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
class SummerWebMvcConfigurer(private val formkeyService: IFormkeyService,
                             private val messageSource: MessageSource,
                             private val summerWebProperties: SummerWebProperties
) : WebMvcConfigurer {

    override fun addInterceptors(registry: InterceptorRegistry) {
        registry.addInterceptor(
                FormDuplicateCheckInterceptor(formkeyService, summerWebProperties.formKeyName))
                .order(Ordered.LOWEST_PRECEDENCE)
        registry.addInterceptor(DeprecatedAPIInterceptor(messageSource))
    }

    /**
     * @param registry 注册转换类
     */
    override fun addFormatters(registry: FormatterRegistry) {
        registry.addConverter(String::class.java, java.util.Date::class.java) { source ->
            if (legalDate(source)) {
                java.util.Date(source.toLong())
            } else {
                null
            }
        }
        registry.addConverter(String::class.java, java.sql.Date::class.java) { source ->
            if (legalDate(source)) {
                java.sql.Date(source.toLong())
            } else {
                null
            }
        }
        registry.addConverter(String::class.java, LocalDate::class.java) { source ->
            if (legalDate(source)) {
                of(source.toLong()).toLocalDate()
            } else {
                null
            }
        }
        registry.addConverter(String::class.java, LocalDateTime::class.java) { source ->
            if (legalDate(source)) {
                of(source.toLong()).toLocalDateTime()
            } else {
                null
            }
        }
        //toString
        registry.addConverter(java.util.Date::class.java, String::class.java) { source ->
            source.time.toString()
        }
        registry.addConverter(java.sql.Date::class.java, String::class.java) { source ->
            source.time.toString()
        }
        registry.addConverter(LocalDate::class.java, String::class.java) { source ->
            of(source).toMillis().toString()
        }
        registry.addConverter(LocalDateTime::class.java, String::class.java) { source ->
            of(source).toMillis().toString()
        }
    }


    private fun legalDate(source: String?): Boolean {
        return !source.isNullOrBlank() && "null" != source && "0" != source
    }

}
