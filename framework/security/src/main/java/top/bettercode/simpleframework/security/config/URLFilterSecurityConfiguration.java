package top.bettercode.simpleframework.security.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;
import top.bettercode.simpleframework.security.IResourceService;
import top.bettercode.simpleframework.security.URLFilterInvocationSecurityMetadataSource;

@Configuration(proxyBeanMethods = false)
@ConditionalOnWebApplication
@ConditionalOnProperty(prefix = "summer.security", name = "enabled", matchIfMissing = true)
public class URLFilterSecurityConfiguration {

  private final ApiSecurityProperties securityProperties;

  public URLFilterSecurityConfiguration(
      ApiSecurityProperties securityProperties) {
    this.securityProperties = securityProperties;
  }

  @Bean
  public URLFilterInvocationSecurityMetadataSource securityMetadataSource(
      IResourceService resourceService,
      RequestMappingHandlerMapping requestMappingHandlerMapping) {
    return new URLFilterInvocationSecurityMetadataSource(resourceService,
        requestMappingHandlerMapping, securityProperties);
  }

}