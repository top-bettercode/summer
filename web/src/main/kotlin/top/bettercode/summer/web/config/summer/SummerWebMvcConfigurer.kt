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
import top.bettercode.summer.tools.lang.serializer.PlusDays
import top.bettercode.summer.tools.lang.util.TimeUtil.Companion.of
import top.bettercode.summer.web.deprecated.DeprecatedAPIInterceptor
import top.bettercode.summer.web.form.FormDuplicateCheckInterceptor
import top.bettercode.summer.web.form.FormkeyService
import top.bettercode.summer.web.properties.SummerWebProperties
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.*


@Configuration(proxyBeanMethods = false)
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
class SummerWebMvcConfigurer(
    private val formkeyService: FormkeyService,
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
        registry.addConverter(dateConverter(
            sourceType = String::class.java,
            targetType = Date::class.java
        ) { source, _, _ ->
            if (legalDate(source)) {
                Date(source.toLong())
            } else {
                null
            }
        })
        registry.addConverter(
            dateConverter(
                sourceType = String::class.java,
                targetType = LocalDate::class.java
            ) { source, _, targetType ->
                if (legalDate(source)) {
                    val localDate = of(source.toLong()).toLocalDate()
                    val plusDays = targetType.getAnnotation(PlusDays::class.java)
                    plusDays?.let {
                        localDate.plusDays(it.value)
                    } ?: localDate
                } else {
                    null
                }
            })
        registry.addConverter(
            dateConverter(
                sourceType = String::class.java,
                targetType = LocalDateTime::class.java
            ) { source, _, _ ->
                if (legalDate(source)) {
                    of(source.toLong()).toLocalDateTime()
                } else {
                    null
                }
            })
        //toString
        registry.addConverter(
            dateConverter(
                sourceType = Date::class.java,
                targetType = String::class.java
            ) { source, _, _ ->
                source.time.toString()
            })
        registry.addConverter(
            dateConverter(
                sourceType = LocalDate::class.java,
                targetType = String::class.java
            ) { source, _, _ -> of(source).toMillis().toString() })
        registry.addConverter(
            dateConverter(
                sourceType = LocalDateTime::class.java,
                targetType = String::class.java
            ) { source, _, _ -> of(source).toMillis().toString() })


        registry.addConverter(
            object : ConditionalGenericConverter {
                override fun getConvertibleTypes(): Set<GenericConverter.ConvertiblePair> {
                    return setOf(
                        GenericConverter.ConvertiblePair(
                            String::class.java,
                            LocalDate::class.java
                        )
                    )
                }

                override fun convert(
                    source: Any?,
                    sourceType: TypeDescriptor,
                    targetType: TypeDescriptor
                ): Any? {
                    source as String?
                    if (source.isNullOrBlank()) {
                        return null
                    } else {
                        val dateTimeFormat = targetType.getAnnotation(DateTimeFormat::class.java)!!
                        val plusDays = targetType.getAnnotation(PlusDays::class.java)!!
                        val formatter = DateTimeFormatter.ofPattern(dateTimeFormat.pattern)
                        val date = LocalDate.parse(source, formatter)
                        return date.plusDays(plusDays.value)
                    }
                }

                override fun matches(
                    sourceType: TypeDescriptor,
                    targetType: TypeDescriptor
                ): Boolean {
                    return targetType.hasAnnotation(DateTimeFormat::class.java) &&
                            targetType.hasAnnotation(PlusDays::class.java)
                }
            })
    }

    private fun <S, T> dateConverter(
        sourceType: Class<S>, targetType: Class<T>,
        convert: (S, TypeDescriptor, TypeDescriptor) -> T?
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
            return convert(source as S, sourceType, targetType)
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
