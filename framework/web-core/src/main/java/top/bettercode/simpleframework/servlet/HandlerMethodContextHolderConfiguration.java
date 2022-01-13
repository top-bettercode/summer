package top.bettercode.simpleframework.servlet;

import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

@Configuration(proxyBeanMethods = false)
@ConditionalOnWebApplication
public class HandlerMethodContextHolderConfiguration {


  public HandlerMethodContextHolderConfiguration(
      RequestMappingHandlerMapping requestMappingHandlerMapping) {
    HandlerMethodContextHolder.setHandlerMapping(requestMappingHandlerMapping);
  }


}