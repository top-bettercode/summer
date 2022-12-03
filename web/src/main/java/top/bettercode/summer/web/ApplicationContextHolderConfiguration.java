package top.bettercode.summer.web;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import top.bettercode.summer.web.support.ApplicationContextHolder;

@Configuration(proxyBeanMethods = false)
public class ApplicationContextHolderConfiguration {

  @Bean
  public ApplicationContextHolder applicationContextHolder() {
    return new ApplicationContextHolder();
  }
}