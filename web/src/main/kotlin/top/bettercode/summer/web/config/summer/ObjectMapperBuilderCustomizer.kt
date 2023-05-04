package top.bettercode.summer.web.config.summer

import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.*
import com.fasterxml.jackson.databind.module.SimpleModule
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.support.AbstractBeanDefinition
import org.springframework.boot.autoconfigure.AutoConfigureBefore
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication
import org.springframework.boot.autoconfigure.jackson.Jackson2ObjectMapperBuilderCustomizer
import org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.support.GenericApplicationContext
import org.springframework.core.annotation.AnnotatedElementUtils
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder
import org.springframework.util.StringUtils
import top.bettercode.summer.tools.lang.util.TimeUtil.Companion.of
import top.bettercode.summer.web.*
import top.bettercode.summer.web.error.*
import top.bettercode.summer.web.filter.*
import top.bettercode.summer.web.properties.JacksonExtProperties
import top.bettercode.summer.web.serializer.MixIn
import top.bettercode.summer.web.support.packagescan.PackageScanClassResolver
import java.io.IOException
import java.lang.reflect.ParameterizedType
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.*

@ConditionalOnProperty(prefix = "summer.web", name = ["enable"], havingValue = "true", matchIfMissing = true)
@Configuration(proxyBeanMethods = false)
@AutoConfigureBefore(JacksonAutoConfiguration::class)
@ConditionalOnWebApplication
class ObjectMapperBuilderCustomizer : Jackson2ObjectMapperBuilderCustomizer {

    private val log = LoggerFactory.getLogger(ObjectMapperBuilderCustomizer::class.java)

    @Bean
    @Throws(ClassNotFoundException::class)
    fun module(applicationContext: GenericApplicationContext,
               packageScanClassResolver: PackageScanClassResolver,
               jacksonExtProperties: JacksonExtProperties): Module {
        val module = SimpleModule()
        val packages: MutableSet<String> = HashSet(
                listOf(*jacksonExtProperties.mixInAnnotationBasePackages))
        val beanNames = applicationContext.getBeanNamesForAnnotation(SpringBootApplication::class.java)
        for (beanName in beanNames) {
            val beanDefinition = applicationContext.getBeanDefinition(
                    beanName) as AbstractBeanDefinition
            if (!beanDefinition.hasBeanClass()) {
                beanDefinition.resolveBeanClass(
                        WebMvcConfiguration::class.java.classLoader)
            }
            val beanClass = beanDefinition.beanClass
            val annotation = AnnotatedElementUtils.findMergedAnnotation(beanClass,
                    SpringBootApplication::class.java)!!
            for (packageClass in Objects.requireNonNull<SpringBootApplication>(annotation).scanBasePackageClasses) {
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

    override fun customize(jacksonObjectMapperBuilder: Jackson2ObjectMapperBuilder) {

        jacksonObjectMapperBuilder.featuresToEnable(JsonGenerator.Feature.WRITE_BIGDECIMAL_AS_PLAIN)

        //LocalDate 配置
        jacksonObjectMapperBuilder.serializerByType(LocalDate::class.java, object : JsonSerializer<LocalDate?>() {
            @Throws(IOException::class)
            override fun serialize(value: LocalDate?, gen: JsonGenerator, serializers: SerializerProvider) {
                gen.writeNumber(of(value!!).toMillis())
            }
        })
        jacksonObjectMapperBuilder.deserializerByType(LocalDate::class.java,
                object : JsonDeserializer<LocalDate?>() {
                    @Throws(IOException::class)
                    override fun deserialize(p: JsonParser, ctxt: DeserializationContext): LocalDate? {
                        val asString = p.valueAsString
                        return if (StringUtils.hasText(asString)) {
                            of(asString.toLong()).toLocalDate()
                        } else {
                            null
                        }
                    }
                })
        //LocalDateTime 配置
        jacksonObjectMapperBuilder
                .serializerByType(LocalDateTime::class.java, object : JsonSerializer<LocalDateTime?>() {
                    @Throws(IOException::class)
                    override fun serialize(value: LocalDateTime?, gen: JsonGenerator,
                                           serializers: SerializerProvider) {
                        gen.writeNumber(of(value!!).toMillis())
                    }
                })
        jacksonObjectMapperBuilder.deserializerByType(LocalDateTime::class.java,
                object : JsonDeserializer<LocalDateTime?>() {
                    @Throws(IOException::class)
                    override fun deserialize(p: JsonParser, ctxt: DeserializationContext): LocalDateTime? {
                        val asString = p.valueAsString
                        return if (StringUtils.hasText(asString)) {
                            of(asString.toLong()).toLocalDateTime()
                        } else {
                            null
                        }
                    }
                })
    }
}
