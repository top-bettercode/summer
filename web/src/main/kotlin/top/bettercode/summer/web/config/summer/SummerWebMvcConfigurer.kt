package top.bettercode.summer.web.config.summer

import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication
import org.springframework.context.MessageSource
import org.springframework.context.annotation.Configuration
import org.springframework.core.Ordered
import org.springframework.core.convert.TypeDescriptor
import org.springframework.core.convert.converter.ConditionalGenericConverter
import org.springframework.core.convert.converter.GenericConverter
import org.springframework.format.FormatterRegistry
import org.springframework.format.annotation.DateTimeFormat
import org.springframework.web.servlet.config.annotation.InterceptorRegistry
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer
import top.bettercode.summer.tools.lang.util.TimeUtil.Companion.of
import top.bettercode.summer.web.deprecated.DeprecatedAPIInterceptor
import top.bettercode.summer.web.form.FormDuplicateCheckInterceptor
import top.bettercode.summer.web.form.IFormkeyService
import top.bettercode.summer.web.properties.SummerWebProperties
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.*

@Configuration(proxyBeanMethods = false)
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
class SummerWebMvcConfigurer(
    private val formkeyService: IFormkeyService,
    private val messageSource: MessageSource,
    private val summerWebProperties: SummerWebProperties
) : WebMvcConfigurer {

    override fun addInterceptors(registry: InterceptorRegistry) {
        registry.addInterceptor(
            FormDuplicateCheckInterceptor(formkeyService, summerWebProperties.formKeyName)
        )
            .order(Ordered.LOWEST_PRECEDENCE)
        registry.addInterceptor(DeprecatedAPIInterceptor(messageSource))
    }

    /**
     * @param registry 注册转换类
     */
    override fun addFormatters(registry: FormatterRegistry) {
        registry.addConverter(genericConverter(
            sourceType = String::class.java,
            targetType = Date::class.java
        ) {
            if (legalDate(it)) {
                Date(it.toLong())
            } else {
                null
            }
        })
        registry.addConverter(
            genericConverter(
                sourceType = String::class.java,
                targetType = LocalDate::class.java
            ) { source ->
                if (legalDate(source)) {
                    of(source.toLong()).toLocalDate()
                } else {
                    null
                }
            })
        registry.addConverter(
            genericConverter(
                sourceType = String::class.java,
                targetType = LocalDateTime::class.java
            ) { source ->
                if (legalDate(source)) {
                    of(source.toLong()).toLocalDateTime()
                } else {
                    null
                }
            })
        //toString
        registry.addConverter(
            genericConverter(
                sourceType = Date::class.java,
                targetType = String::class.java
            ) { source ->
                source.time.toString()
            })
        registry.addConverter(
            genericConverter(
                sourceType = LocalDate::class.java,
                targetType = String::class.java
            ) { source -> of(source).toMillis().toString() })
        registry.addConverter(
            genericConverter(
                sourceType = LocalDateTime::class.java,
                targetType = String::class.java
            ) { source -> of(source).toMillis().toString() })
    }

    private fun <S, T> genericConverter(
        sourceType: Class<S>, targetType: Class<T>,
        convert: (S) -> T?
    ) = object : ConditionalGenericConverter {

        override fun getConvertibleTypes(): Set<GenericConverter.ConvertiblePair> {
            return setOf(GenericConverter.ConvertiblePair(sourceType, targetType))
        }

        override fun convert(
            source: Any?,
            sourceType: TypeDescriptor,
            targetType: TypeDescriptor
        ): Any? {
            if (source == null) return null
            @Suppress("UNCHECKED_CAST")
            return convert(source as S)
        }

        override fun matches(sourceType: TypeDescriptor, targetType: TypeDescriptor): Boolean {
            return !targetType.hasAnnotation(DateTimeFormat::class.java) &&
                    !sourceType.hasAnnotation(DateTimeFormat::class.java)
        }
    }


    private fun legalDate(source: String?): Boolean {
        return !source.isNullOrBlank() && "null" != source && "0" != source
    }

}
