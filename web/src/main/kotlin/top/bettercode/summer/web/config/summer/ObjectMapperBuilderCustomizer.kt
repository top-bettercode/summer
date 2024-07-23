package top.bettercode.summer.web.config.summer

import com.fasterxml.jackson.annotation.JsonFormat
import com.fasterxml.jackson.databind.Module
import com.fasterxml.jackson.databind.module.SimpleModule
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.beans.factory.support.AbstractBeanDefinition
import org.springframework.boot.autoconfigure.AutoConfigureBefore
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication
import org.springframework.boot.autoconfigure.jackson.Jackson2ObjectMapperBuilderCustomizer
import org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.support.GenericApplicationContext
import org.springframework.core.annotation.AnnotatedElementUtils
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder
import top.bettercode.summer.tools.lang.util.StringUtil
import top.bettercode.summer.web.properties.JacksonExtProperties
import top.bettercode.summer.web.serializer.MixIn
import top.bettercode.summer.web.support.packagescan.PackageScanClassResolver
import java.lang.reflect.ParameterizedType
import java.time.LocalDate
import java.time.LocalDateTime


@Configuration(proxyBeanMethods = false)
@AutoConfigureBefore(JacksonAutoConfiguration::class)
@ConditionalOnWebApplication
class ObjectMapperBuilderCustomizer {

    private val log = LoggerFactory.getLogger(ObjectMapperBuilderCustomizer::class.java)

    @Bean
    fun customizer(): Jackson2ObjectMapperBuilderCustomizer {
        return Jackson2ObjectMapperBuilderCustomizer { builder: Jackson2ObjectMapperBuilder ->
            builder.postConfigurer {
                val value = JsonFormat.Value.forShape( JsonFormat.Shape.NUMBER ).withLenient(true)

                it.configOverride(LocalDate::class.java).format = value
                it.configOverride(LocalDateTime::class.java).format = value
            }
        }
    }


    @Bean
    fun timeModule(@Value("\${spring.jackson.serialization.WRITE_DATES_AS_TIMESTAMPS:true}") writeDatesAsTimestamps: Boolean): Module {
        return StringUtil.timeModule(writeDatesAsTimestamps)
    }

    @Bean
    fun module(
        applicationContext: GenericApplicationContext,
        packageScanClassResolver: PackageScanClassResolver,
        jacksonExtProperties: JacksonExtProperties
    ): Module {
        val module = SimpleModule()
        val packages: MutableSet<String> = HashSet(
            listOf(*jacksonExtProperties.mixInAnnotationBasePackages)
        )
        val beanNames =
            applicationContext.getBeanNamesForAnnotation(SpringBootApplication::class.java)
        for (beanName in beanNames) {
            val beanDefinition = applicationContext.getBeanDefinition(
                beanName
            ) as AbstractBeanDefinition
            if (!beanDefinition.hasBeanClass()) {
                beanDefinition.resolveBeanClass(
                    WebMvcConfiguration::class.java.classLoader
                )
            }
            val beanClass = beanDefinition.beanClass
            val annotation = AnnotatedElementUtils.findMergedAnnotation(
                beanClass,
                SpringBootApplication::class.java
            )!!
            for (packageClass in annotation.scanBasePackageClasses) {
                packages.add(packageClass::class.java.getPackage().name)
            }
            packages.addAll(listOf(*annotation.scanBasePackages))
            packages.add(beanClass.getPackage().name)
        }
        val allSubClasses = packageScanClassResolver
            .findImplementations(MixIn::class.java, *packages.toTypedArray<String>())
        val targetTypes = HashMap<Class<*>, Class<*>>()
        for (clazz in allSubClasses) {
            val `object` = clazz.genericInterfaces[0] as ParameterizedType
            val targetType = `object`.actualTypeArguments[0] as Class<*>
            if (targetTypes.containsKey(targetType)) {
                throw Error(targetType.toString() + " 已存在对应Json MixIn: " + targetTypes[targetType])
            }
            targetTypes[targetType] = clazz
            if (log.isTraceEnabled) {
                log.trace("Detected MixInAnnotation:{}=>{}", targetType, clazz)
            }
            module.setMixInAnnotation(targetType, clazz)
        }
        return module
    }

}
