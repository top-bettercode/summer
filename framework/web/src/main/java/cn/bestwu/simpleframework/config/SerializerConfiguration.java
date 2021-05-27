package cn.bestwu.simpleframework.config;

import cn.bestwu.lang.property.PropertiesSource;
import cn.bestwu.lang.util.LocalDateTimeHelper;
import cn.bestwu.simpleframework.support.code.CodeService;
import cn.bestwu.simpleframework.support.code.CodeTypes;
import cn.bestwu.simpleframework.support.code.ICodeService;
import cn.bestwu.simpleframework.support.packagescan.PackageScanClassResolver;
import cn.bestwu.simpleframework.web.serializer.CodeSerializer;
import cn.bestwu.simpleframework.web.serializer.CustomNullSerializerModifier;
import cn.bestwu.simpleframework.web.serializer.MixIn;
import cn.bestwu.simpleframework.web.serializer.UrlSerializer;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.Module;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import java.io.IOException;
import java.lang.reflect.ParameterizedType;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.autoconfigure.jackson.Jackson2ObjectMapperBuilderCustomizer;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.core.env.Environment;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;
import org.springframework.http.converter.xml.MappingJackson2XmlHttpMessageConverter;
import org.springframework.util.ClassUtils;

/**
 * @author Peter Wu
 */
@Configuration
@ConditionalOnWebApplication
@EnableConfigurationProperties({JacksonExtProperties.class})
@Order(Ordered.HIGHEST_PRECEDENCE)
public class SerializerConfiguration {

  private final Logger log = LoggerFactory.getLogger(SerializerConfiguration.class);
  private final JacksonExtProperties jacksonExtProperties;

  public SerializerConfiguration(
      @Autowired(required = false) MappingJackson2XmlHttpMessageConverter xmlHttpMessageConverter,
      ObjectMapper objectMapper,
      JacksonExtProperties jacksonExtProperties) {
    this.jacksonExtProperties = jacksonExtProperties;
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

  @Bean
  public Module module(ApplicationContext applicationContext,
      PackageScanClassResolver packageScanClassResolver) {
    SimpleModule module = new SimpleModule();
    Set<String> packages = PackageScanClassResolver
        .detectPackagesToScan(applicationContext,
            jacksonExtProperties.getMixInAnnotationBasePackages());

    packages.add("cn.bestwu.simpleframework.data.serializer");

    Set<Class<?>> allSubClasses = packageScanClassResolver
        .findImplementations(MixIn.class, packages.toArray(new String[0]));
    for (Class<?> aClass : allSubClasses) {
      try {
        ParameterizedType object = (ParameterizedType) aClass.getGenericInterfaces()[0];
        Class<?> targetType = (Class<?>) object.getActualTypeArguments()[0];
        if (log.isTraceEnabled()) {
          log.trace("Detected MixInAnnotation:{}=>{}", targetType, aClass);
        }
        module.setMixInAnnotation(targetType, aClass);
      } catch (Exception e) {
        log.warn(aClass + "Detected fail", e);
      }
    }
    return module;
  }

  @Configuration
  @ConditionalOnWebApplication
  protected static class ObjectMapperBuilderCustomizer implements
      Jackson2ObjectMapperBuilderCustomizer {

    @Override
    public void customize(Jackson2ObjectMapperBuilder jacksonObjectMapperBuilder) {
      jacksonObjectMapperBuilder.featuresToEnable(JsonGenerator.Feature.WRITE_BIGDECIMAL_AS_PLAIN);
      jacksonObjectMapperBuilder.serializerByType(LocalDate.class, new JsonSerializer<LocalDate>() {
        @Override
        public void serialize(LocalDate value, JsonGenerator gen, SerializerProvider serializers)
            throws IOException {
          gen.writeNumber(LocalDateTimeHelper.of(value).toMillis());
        }
      });
      jacksonObjectMapperBuilder
          .serializerByType(LocalDateTime.class, new JsonSerializer<LocalDateTime>() {
            @Override
            public void serialize(LocalDateTime value, JsonGenerator gen,
                SerializerProvider serializers)
                throws IOException {
              gen.writeNumber(LocalDateTimeHelper.of(value).toMillis());
            }
          });
    }
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
    return new CodeService(new PropertiesSource("default-dic-code", "dic-code"));
  }

  @Configuration
  @ConditionalOnWebApplication
  protected static class CodeSerializerConfiguration {

    public CodeSerializerConfiguration(ICodeService codeService) {
      CodeSerializer.setCodeService(codeService);
      CodeTypes.setCodeService(codeService);
    }
  }

}
