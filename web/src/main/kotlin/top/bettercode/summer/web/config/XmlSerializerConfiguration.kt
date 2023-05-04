package top.bettercode.summer.web.config

import com.fasterxml.jackson.dataformat.xml.XmlMapper
import com.fasterxml.jackson.dataformat.xml.ser.ToXmlGenerator
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication
import org.springframework.context.annotation.Configuration
import org.springframework.core.Ordered
import org.springframework.core.annotation.Order
import org.springframework.http.converter.xml.MappingJackson2XmlHttpMessageConverter
import top.bettercode.summer.web.properties.JacksonExtProperties

/**
 * @author Peter Wu
 */
@Configuration(proxyBeanMethods = false)
@ConditionalOnWebApplication
@ConditionalOnClass(XmlMapper::class)
@Order(Ordered.HIGHEST_PRECEDENCE)
class XmlSerializerConfiguration(
        @Autowired(required = false) xmlHttpMessageConverter: MappingJackson2XmlHttpMessageConverter,
        jacksonExtProperties: JacksonExtProperties) {
    init {
        val xmlMapper = xmlHttpMessageConverter.objectMapper as XmlMapper
        xmlMapper.setConfig(
                xmlMapper.serializationConfig.withRootName(jacksonExtProperties.xmlRootName))
        xmlMapper.configure(
                ToXmlGenerator.Feature.WRITE_XML_DECLARATION,
                jacksonExtProperties.isWriteXmlDeclaration)
    }
}
