package top.bettercode.simpleframework.security.config;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import java.util.concurrent.TimeUnit;
import javax.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;
import top.bettercode.simpleframework.security.ApiAuthenticationToken;
import top.bettercode.simpleframework.security.ApiSecurityErrorHandler;
import top.bettercode.simpleframework.security.ApiTokenService;
import top.bettercode.simpleframework.security.IResourceService;
import top.bettercode.simpleframework.security.URLFilterInvocationSecurityMetadataSource;
import top.bettercode.simpleframework.security.UsernameSetRequestLoggingHandler;
import top.bettercode.simpleframework.security.authorization.ApiAuthorizationService;
import top.bettercode.simpleframework.security.authorization.InMemoryApiAuthorizationService;

@Configuration(proxyBeanMethods = false)
@ConditionalOnWebApplication
public class ApiSecurityConfiguration {

  private final Logger log = LoggerFactory.getLogger(SecurityConfiguration.class);
  private final ApiSecurityProperties securityProperties;

  public ApiSecurityConfiguration(
      ApiSecurityProperties securityProperties) {
    this.securityProperties = securityProperties;
  }

  @Bean
  public UsernameSetRequestLoggingHandler usernameSetRequestLoggingHandler() {
    return new UsernameSetRequestLoggingHandler();
  }

  @Bean
  public URLFilterInvocationSecurityMetadataSource securityMetadataSource(
      IResourceService resourceService,
      RequestMappingHandlerMapping requestMappingHandlerMapping) {
    return new URLFilterInvocationSecurityMetadataSource(resourceService,
        requestMappingHandlerMapping, securityProperties);
  }

  @Bean
  public ApiTokenService apiTokenService(ApiAuthorizationService apiAuthorizationService,
      UserDetailsService userDetailsService) {
    return new ApiTokenService(securityProperties, apiAuthorizationService, userDetailsService);
  }

  @Bean
  public ApiSecurityErrorHandler securityOAuth2ErrorHandler(MessageSource messageSource,
      @Autowired(required = false) HttpServletRequest request) {
    return new ApiSecurityErrorHandler(messageSource, request);
  }

  @ConditionalOnMissingBean(ApiAuthorizationService.class)
  @Bean
  public ApiAuthorizationService apiAuthorizationService() {
    Cache<String, ApiAuthenticationToken> build = CacheBuilder.newBuilder()
        .expireAfterWrite(Math.max(securityProperties.getAccessTokenValiditySeconds(),
            securityProperties.getRefreshTokenValiditySeconds()), TimeUnit.SECONDS)
        .maximumSize(10000).build();
    Cache<String, String> accessTokenBuild = CacheBuilder.newBuilder()
        .expireAfterWrite(securityProperties.getAccessTokenValiditySeconds(), TimeUnit.SECONDS)
        .maximumSize(10000).build();
    Cache<String, String> refreshTokenBuild = CacheBuilder.newBuilder()
        .expireAfterWrite(
            securityProperties.getRefreshTokenValiditySeconds(), TimeUnit.SECONDS)
        .maximumSize(10000).build();
    return new InMemoryApiAuthorizationService(build.asMap(), accessTokenBuild.asMap(),
        refreshTokenBuild.asMap());
  }

}