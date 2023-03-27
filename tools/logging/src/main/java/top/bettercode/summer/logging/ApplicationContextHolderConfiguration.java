package top.bettercode.summer.logging;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration(proxyBeanMethods = false)
public class ApplicationContextHolderConfiguration {

  @Bean
  public ApplicationContextHolder applicationContextHolder() {
    return new ApplicationContextHolder();
  }
}