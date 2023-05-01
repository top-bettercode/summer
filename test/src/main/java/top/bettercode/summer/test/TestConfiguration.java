package top.bettercode.summer.test;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import top.bettercode.summer.apisign.ApiSignConfiguration;
import top.bettercode.summer.apisign.ApiSignProperties;

@Configuration(proxyBeanMethods = false)
@ConditionalOnWebApplication
@AutoConfigureAfter(ApiSignConfiguration.class)
public class TestConfiguration {

  @ConditionalOnBean(ApiSignProperties.class)
  @ConditionalOnProperty(prefix = "summer.auto-sign", name = "enabled", matchIfMissing = true)
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