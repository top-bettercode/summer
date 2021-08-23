package top.bettercode.summer.util.test;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.context.request.WebRequest;
import top.bettercode.api.sign.ApiSignProperties;
import top.bettercode.simpleframework.web.error.ErrorAttributes;
import top.bettercode.simpleframework.web.error.MocTestErrorLoggingHandler;

@Configuration(proxyBeanMethods = false)
@ConditionalOnWebApplication
public class TestConfiguration {

  @Bean
  public MocTestErrorLoggingHandler mocTestErrorLoggingHandler(
      ErrorAttributes errorAttributes,
      @Autowired(required = false) WebRequest webRequest) {
    return new MocTestErrorLoggingHandler(errorAttributes, webRequest);
  }

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