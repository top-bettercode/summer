package cn.bestwu.simpleframework.config;

import cn.bestwu.simpleframework.web.serializer.CodeSerializer;
import cn.bestwu.simpleframework.web.serializer.CodeService;
import cn.bestwu.simpleframework.web.serializer.CustomNullSerializerModifier;
import cn.bestwu.simpleframework.web.serializer.ICodeService;
import cn.bestwu.simpleframework.web.serializer.UrlSerializer;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.core.env.Environment;
import org.springframework.http.converter.xml.MappingJackson2XmlHttpMessageConverter;
import org.springframework.util.ClassUtils;

/**
 * @author Peter Wu
 */
@Configuration
@ConditionalOnWebApplication
@Order(Ordered.HIGHEST_PRECEDENCE)
public class SerializerConfiguration {

  public SerializerConfiguration(
      @Autowired(required = false) MappingJackson2XmlHttpMessageConverter xmlHttpMessageConverter,
      @Value("${spring.jackson.write-xml-declaration:false}") Boolean writeXmlDeclaration,
      @Value("${spring.jackson.default-empty:false}") Boolean defaultEmpty,
      @Value("${spring.jackson.xml-root-name:xml}") String xmlRootName, ObjectMapper objectMapper) {
    if (xmlHttpMessageConverter != null && ClassUtils
        .isPresent("com.fasterxml.jackson.dataformat.xml.XmlMapper",
            SerializerConfiguration.class.getClassLoader())) {
      XmlMapper xmlMapper = (XmlMapper) xmlHttpMessageConverter.getObjectMapper();
      xmlMapper.setConfig(xmlMapper.getSerializationConfig().withRootName(xmlRootName));
      xmlMapper.configure(
          com.fasterxml.jackson.dataformat.xml.ser.ToXmlGenerator.Feature.WRITE_XML_DECLARATION,
          writeXmlDeclaration);
    }
    objectMapper.setSerializerFactory(objectMapper.getSerializerFactory()
        .withSerializerModifier(new CustomNullSerializerModifier(defaultEmpty)));
  }

  @Configuration
  @ConditionalOnWebApplication
  protected static class UrlSerializerConfiguration {

    public UrlSerializerConfiguration(Environment environment) {
      UrlSerializer.setEnvironment(environment);
    }
  }

  @ConditionalOnMissingBean
  @Bean
  public ICodeService codeService() {
    return new CodeService();
  }

  @Configuration
  @ConditionalOnWebApplication
  protected static class CodeSerializerConfiguration {

    public CodeSerializerConfiguration(ICodeService codeService) {
      CodeSerializer.setCodeService(codeService);
    }
  }

}
