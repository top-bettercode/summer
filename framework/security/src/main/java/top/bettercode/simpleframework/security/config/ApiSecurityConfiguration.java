package top.bettercode.simpleframework.security.config;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import java.io.IOException;
import java.util.concurrent.TimeUnit;
import javax.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.autoconfigure.jackson.Jackson2ObjectMapperBuilderCustomizer;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;
import org.springframework.security.access.SecurityConfig;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import top.bettercode.simpleframework.security.ApiToken;
import top.bettercode.simpleframework.security.ApiSecurityErrorHandler;
import top.bettercode.simpleframework.security.ApiTokenService;
import top.bettercode.simpleframework.security.DefaultAuthority;
import top.bettercode.simpleframework.security.IResourceService;
import top.bettercode.simpleframework.security.repository.ApiTokenRepository;
import top.bettercode.simpleframework.security.repository.InMemoryApiTokenRepository;

@Configuration(proxyBeanMethods = false)
@ConditionalOnWebApplication
public class ApiSecurityConfiguration {

  private final ApiSecurityProperties securityProperties;

  public ApiSecurityConfiguration(
      ApiSecurityProperties securityProperties) {
    this.securityProperties = securityProperties;
    DefaultAuthority.DEFAULT_AUTHENTICATED = new SecurityConfig(
        securityProperties.getDefaultAuthority());
    DefaultAuthority.DEFAULT_GRANTED_AUTHORITY = new SimpleGrantedAuthority(
        securityProperties.getDefaultAuthority());
  }

  @ConditionalOnMissingBean(IResourceService.class)
  @Bean
  public IResourceService resourceService() {
    return new IResourceService() {
    };
  }

  @ConditionalOnMissingBean(PasswordEncoder.class)
  @Bean
  public PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder();
  }

  @Bean
  public ApiTokenService apiTokenService(ApiTokenRepository apiAuthorizationService,
      UserDetailsService userDetailsService) {
    return new ApiTokenService(securityProperties, apiAuthorizationService, userDetailsService);
  }

  @Bean
  public ApiSecurityErrorHandler securityOAuth2ErrorHandler(MessageSource messageSource,
      @Autowired(required = false) HttpServletRequest request) {
    return new ApiSecurityErrorHandler(messageSource, request);
  }

  @ConditionalOnMissingBean(ApiTokenRepository.class)
  @Bean
  public ApiTokenRepository apiAuthorizationService() {
    Cache<String, ApiToken> cache = Caffeine.newBuilder()
        .expireAfterWrite(Math.max(securityProperties.getAccessTokenValiditySeconds(),
            securityProperties.getRefreshTokenValiditySeconds()), TimeUnit.SECONDS)
        .maximumSize(10000).build();
    Cache<String, String> accessTokenBuild = Caffeine.newBuilder()
        .expireAfterWrite(securityProperties.getAccessTokenValiditySeconds(), TimeUnit.SECONDS)
        .maximumSize(10000).build();
    Cache<String, String> refreshTokenBuild = Caffeine.newBuilder()
        .expireAfterWrite(
            securityProperties.getRefreshTokenValiditySeconds(), TimeUnit.SECONDS)
        .maximumSize(10000).build();
    return new InMemoryApiTokenRepository(cache.asMap(), accessTokenBuild.asMap(),
        refreshTokenBuild.asMap());
  }

  @Configuration(proxyBeanMethods = false)
  @ConditionalOnWebApplication
  protected static class ObjectMapperBuilderCustomizer implements
      Jackson2ObjectMapperBuilderCustomizer {

    @Override
    public void customize(Jackson2ObjectMapperBuilder jacksonObjectMapperBuilder) {
      jacksonObjectMapperBuilder.serializerByType(GrantedAuthority.class,
          new JsonSerializer<GrantedAuthority>() {
            @Override
            public void serialize(GrantedAuthority value, JsonGenerator gen,
                SerializerProvider serializers)
                throws IOException {
              gen.writeString(value.getAuthority());
            }
          });
    }
  }

}