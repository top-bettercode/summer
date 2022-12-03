package top.bettercode.summer.web.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.core.env.Environment;
import top.bettercode.summer.web.serializer.CustomNullSerializerModifier;
import top.bettercode.summer.web.serializer.UrlSerializer;
import top.bettercode.summer.web.support.code.CodeServiceHolder;
import top.bettercode.summer.web.support.code.ICodeService;

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
