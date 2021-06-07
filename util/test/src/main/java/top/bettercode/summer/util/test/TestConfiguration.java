package top.bettercode.summer.util.test;

import top.bettercode.api.sign.ApiSignAlgorithm;
import top.bettercode.api.sign.ApiSignProperties;
import top.bettercode.simpleframework.web.error.ErrorAttributes;
import top.bettercode.simpleframework.web.error.MocTestErrorLoggingHandler;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.context.request.WebRequest;

@Configuration(proxyBeanMethods = false)
@ConditionalOnWebApplication
public class TestConfiguration {

  @Bean
  public MocTestErrorLoggingHandler mocTestErrorLoggingHandler(
      ErrorAttributes errorAttributes,
      @Autowired(required = false) WebRequest webRequest) {
    return new MocTestErrorLoggingHandler(errorAttributes, webRequest);
  }

  @ConditionalOnBean(ApiSignAlgorithm.class)
  @Bean
  public AutoSignRequestHandler autoSignRequestHandler(ApiSignProperties apiSignProperties,
      ApiSignAlgorithm apiSignAlgorithm) {
    return new AutoSignRequestHandler(apiSignProperties, apiSignAlgorithm);
  }

  @Bean
  public AutoDocFilter autoSignFilter(
      @Autowired(required = false) List<AutoDocRequestHandler> handlers) {
    return new AutoDocFilter(handlers);
  }

}