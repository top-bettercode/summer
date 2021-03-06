package top.bettercode.summer.util.test;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import top.bettercode.api.sign.ApiSignProperties;

@Configuration(proxyBeanMethods = false)
@ConditionalOnWebApplication
public class TestConfiguration {

  @ConditionalOnBean(ApiSignProperties.class)
  @Bean
  public AutoSignRequestHandler autoSignRequestHandler(ApiSignProperties apiSignProperties) {
    return new AutoSignRequestHandler(apiSignProperties);
  }

  @Bean
  public AutoDocFilter autoSignFilter(
      @Autowired(required = false) List<AutoDocRequestHandler> handlers) {
    return new AutoDocFilter(handlers);
  }

}