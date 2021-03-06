package top.bettercode.simpleframework;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import top.bettercode.simpleframework.support.ApplicationContextHolder;

@Configuration(proxyBeanMethods = false)
public class ApplicationContextHolderConfiguration {

  @Bean
  public ApplicationContextHolder applicationContextHolder() {
    return new ApplicationContextHolder();
  }
}