package top.bettercode.summer.web.config;

import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.converter.xml.MappingJackson2XmlHttpMessageConverter;

/**
 * @author Peter Wu
 */
@Configuration(proxyBeanMethods = false)
@ConditionalOnWebApplication
@ConditionalOnClass(XmlMapper.class)
@EnableConfigurationProperties({JacksonExtProperties.class})
@Order(Ordered.HIGHEST_PRECEDENCE)
public class XmlSerializerConfiguration {


    public XmlSerializerConfiguration(
            @Autowired(required = false) MappingJackson2XmlHttpMessageConverter xmlHttpMessageConverter,
            JacksonExtProperties jacksonExtProperties) {
        XmlMapper xmlMapper = (XmlMapper) xmlHttpMessageConverter.getObjectMapper();
        xmlMapper.setConfig(
                xmlMapper.getSerializationConfig().withRootName(jacksonExtProperties.getXmlRootName()));
        xmlMapper.configure(
                com.fasterxml.jackson.dataformat.xml.ser.ToXmlGenerator.Feature.WRITE_XML_DECLARATION,
                jacksonExtProperties.getWriteXmlDeclaration());
    }


}
