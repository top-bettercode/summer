package top.bettercode.simpleframework.config;

import top.bettercode.lang.property.PropertiesSource;
import top.bettercode.simpleframework.support.code.CodeService;
import top.bettercode.simpleframework.support.code.CodeTypes;
import top.bettercode.simpleframework.support.code.ICodeService;
import top.bettercode.simpleframework.web.serializer.CodeSerializer;
import top.bettercode.simpleframework.web.serializer.CustomNullSerializerModifier;
import top.bettercode.simpleframework.web.serializer.UrlSerializer;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import org.springframework.beans.factory.annotation.Autowired;
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
@Configuration(proxyBeanMethods = false)
@ConditionalOnWebApplication
@Order(Ordered.HIGHEST_PRECEDENCE)
public class SerializerConfiguration {


  public SerializerConfiguration(
      @Autowired(required = false) MappingJackson2XmlHttpMessageConverter xmlHttpMessageConverter,
      ObjectMapper objectMapper,
      JacksonExtProperties jacksonExtProperties) {
    if (xmlHttpMessageConverter != null && ClassUtils
        .isPresent("com.fasterxml.jackson.dataformat.xml.XmlMapper",
            SerializerConfiguration.class.getClassLoader())) {
      XmlMapper xmlMapper = (XmlMapper) xmlHttpMessageConverter.getObjectMapper();
      xmlMapper.setConfig(
          xmlMapper.getSerializationConfig().withRootName(jacksonExtProperties.getXmlRootName()));
      xmlMapper.configure(
          com.fasterxml.jackson.dataformat.xml.ser.ToXmlGenerator.Feature.WRITE_XML_DECLARATION,
          jacksonExtProperties.getWriteXmlDeclaration());
    }
    objectMapper.setSerializerFactory(objectMapper.getSerializerFactory()
        .withSerializerModifier(
            new CustomNullSerializerModifier(jacksonExtProperties)));
  }


  @Configuration(proxyBeanMethods = false)
  @ConditionalOnWebApplication
  protected static class UrlSerializerConfiguration {

    public UrlSerializerConfiguration(Environment environment) {
      UrlSerializer.setEnvironment(environment);
    }
  }

  @ConditionalOnMissingBean
  @Bean
  public ICodeService codeService() {
    return new CodeService(new PropertiesSource("default-dic-code", "dic-code"));
  }

  @Configuration(proxyBeanMethods = false)
  @ConditionalOnWebApplication
  protected static class CodeSerializerConfiguration {

    public CodeSerializerConfiguration(ICodeService codeService) {
      CodeSerializer.setCodeService(codeService);
      CodeTypes.setCodeService(codeService);
    }
  }

}
