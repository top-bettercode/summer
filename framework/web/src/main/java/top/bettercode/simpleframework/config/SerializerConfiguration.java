package top.bettercode.simpleframework.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.core.env.Environment;
import top.bettercode.simpleframework.support.code.CodeServiceHolder;
import top.bettercode.simpleframework.support.code.ICodeService;
import top.bettercode.simpleframework.web.serializer.CustomNullSerializerModifier;
import top.bettercode.simpleframework.web.serializer.UrlSerializer;

@Configuration(proxyBeanMethods = false)
@ConditionalOnWebApplication
@Order(Ordered.HIGHEST_PRECEDENCE)
public class SerializerConfiguration {

  public SerializerConfiguration(Environment environment,
      ObjectMapper objectMapper,
      JacksonExtProperties jacksonExtProperties) {
    UrlSerializer.setEnvironment(environment);
    objectMapper.setSerializerFactory(objectMapper.getSerializerFactory()
        .withSerializerModifier(
            new CustomNullSerializerModifier(jacksonExtProperties)));
  }

  @Bean("defaultCodeService")
  public ICodeService defaultCodeService() {
    return CodeServiceHolder.PROPERTIES_CODESERVICE;
  }

}
