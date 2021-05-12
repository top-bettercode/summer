package cn.bestwu.summer.util.test;

import cn.bestwu.api.sign.ApiSignAlgorithm;
import cn.bestwu.api.sign.ApiSignProperties;
import cn.bestwu.simpleframework.web.error.ErrorAttributes;
import cn.bestwu.simpleframework.web.error.MocTestErrorLoggingHandler;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.context.request.WebRequest;

@Configuration
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