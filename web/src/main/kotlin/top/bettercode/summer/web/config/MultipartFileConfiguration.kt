package top.bettercode.summer.web.config

import org.slf4j.LoggerFactory
import org.springframework.boot.autoconfigure.AutoConfigureBefore
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication
import org.springframework.boot.autoconfigure.web.WebProperties
import org.springframework.boot.autoconfigure.web.servlet.MultipartAutoConfiguration
import org.springframework.boot.autoconfigure.web.servlet.MultipartProperties
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.Ordered
import org.springframework.core.annotation.Order
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer
import top.bettercode.summer.web.properties.SummerMultipartProperties
import top.bettercode.summer.web.resolver.multipart.MuipartFileToAttachmentConverter
import java.io.File
import javax.servlet.MultipartConfigElement

/**
 * 文件上传 配置
 *
 * @author Peter Wu
 */
@ConditionalOnProperty(prefix = "summer.multipart", value = ["base-save-path"])
@EnableConfigurationProperties(SummerMultipartProperties::class, MultipartProperties::class)
@Configuration(proxyBeanMethods = false)
@ConditionalOnWebApplication
@AutoConfigureBefore(MultipartAutoConfiguration::class)
@Order(Ordered.HIGHEST_PRECEDENCE)
class MultipartFileConfiguration(
        multipartProperties: SummerMultipartProperties,
        webProperties: WebProperties) : WebMvcConfigurer {
    private val log = LoggerFactory.getLogger(MultipartFileConfiguration::class.java)

    init {
        val resources = webProperties.resources
        resources.staticLocations = resources.staticLocations.plus(multipartProperties.staticLocations)
    }

    @Bean
    fun multipartConfigElement(
            multipartProperties: MultipartProperties,
            properties: SummerMultipartProperties): MultipartConfigElement {
        val file = File(properties.baseSavePath, "tmp").absoluteFile
        multipartProperties.location = file.absolutePath
        if (!file.exists() && !file.mkdirs()) {
            log.error("创建临时目录失败：{}", file.absolutePath)
        }
        return multipartProperties.createMultipartConfig()
    }

    @Bean
    fun muipartFileToAttachmentConverter(
            multipartProperties: SummerMultipartProperties): MuipartFileToAttachmentConverter {
        return MuipartFileToAttachmentConverter(multipartProperties)
    }
}
