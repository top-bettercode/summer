package top.bettercode.simpleframework.security.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import java.util.Collection;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.stream.Collectors;
import javax.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.AutoConfigureOrder;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.security.access.AccessDecisionManager;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.ConfigAttribute;
import org.springframework.security.config.annotation.ObjectPostProcessor;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.web.access.intercept.FilterSecurityInterceptor;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.util.StringUtils;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;
import top.bettercode.simpleframework.config.CorsProperties;
import top.bettercode.simpleframework.config.SummerWebProperties;
import top.bettercode.simpleframework.security.ApiAuthenticationToken;
import top.bettercode.simpleframework.security.authorization.ApiAuthorizationService;
import top.bettercode.simpleframework.security.ApiSecurityErrorHandler;
import top.bettercode.simpleframework.security.ApiTokenBuild;
import top.bettercode.simpleframework.security.ApiTokenEndpointFilter;
import top.bettercode.simpleframework.security.IResourceService;
import top.bettercode.simpleframework.security.IRevokeTokenService;
import top.bettercode.simpleframework.security.authorization.InMemoryApiAuthorizationService;
import top.bettercode.simpleframework.security.URLFilterInvocationSecurityMetadataSource;
import top.bettercode.simpleframework.security.UserDetailsAuthenticationProvider;

@Configuration(proxyBeanMethods = false)
@EnableWebSecurity
@AutoConfigureOrder(Ordered.HIGHEST_PRECEDENCE + 10)
@ConditionalOnWebApplication
@EnableConfigurationProperties({ApiSecurityProperties.class, CorsProperties.class})
public class ApiSecurityConfiguration extends WebSecurityConfigurerAdapter {

  private final ApiSecurityProperties securityProperties;
  private final CorsProperties corsProperties;
  private final URLFilterInvocationSecurityMetadataSource securityMetadataSource;
  private final AccessDecisionManager accessDecisionManager;
  private final ApiTokenBuild apiTokenBuild;
  private final IRevokeTokenService revokeTokenService;
  private final SummerWebProperties summerWebProperties;
  private final ObjectMapper objectMapper;
  private final ApiAuthorizationService apiAuthorizationService;

  public ApiSecurityConfiguration(
      ApiSecurityProperties securityProperties,
      CorsProperties corsProperties,
      URLFilterInvocationSecurityMetadataSource securityMetadataSource,
      AccessDecisionManager accessDecisionManager,
      ApiTokenBuild apiTokenBuild,
      @Autowired(required = false) IRevokeTokenService revokeTokenService,
      SummerWebProperties summerWebProperties,
      ObjectMapper objectMapper,
      ApiAuthorizationService apiAuthorizationService) {
    this.securityProperties = securityProperties;
    this.corsProperties = corsProperties;
    this.securityMetadataSource = securityMetadataSource;
    this.accessDecisionManager = accessDecisionManager;
    this.apiTokenBuild = apiTokenBuild;
    this.revokeTokenService = revokeTokenService;
    this.summerWebProperties = summerWebProperties;
    this.objectMapper = objectMapper;
    this.apiAuthorizationService = apiAuthorizationService;
  }


  @Override
  public void configure(HttpSecurity http) throws Exception {
    if (securityProperties.getSupportClientCache()) {
      http.headers().cacheControl().disable();
    }
    if (securityProperties.getFrameOptionsDisable()) {
      http.headers().frameOptions().disable();
    }

    if (corsProperties.isEnable()) {
      http.cors();
    }

    http.csrf().disable();

    ApiTokenEndpointFilter apiTokenEndpointFilter = new ApiTokenEndpointFilter(
        authenticationManagerBean(), apiAuthorizationService, apiTokenBuild,
        summerWebProperties, revokeTokenService, securityProperties, objectMapper);

    http.authenticationProvider(new UserDetailsAuthenticationProvider());
    http.addFilterBefore(apiTokenEndpointFilter, UsernamePasswordAuthenticationFilter.class);

    http
        .sessionManagement().sessionCreationPolicy(securityProperties.getSessionCreationPolicy())
        .and().exceptionHandling(config -> {
          config.accessDeniedHandler((request, response, accessDeniedException) -> {
            throw accessDeniedException;
          });
          config.authenticationEntryPoint((request, response, authException) -> {
            throw authException;
          });
        })
        .authorizeRequests()
        .withObjectPostProcessor(new ObjectPostProcessor<FilterSecurityInterceptor>() {
          public <O extends FilterSecurityInterceptor> O postProcess(
              O fsi) {
            fsi.setSecurityMetadataSource(securityMetadataSource);
            fsi.setAccessDecisionManager(accessDecisionManager);
            return fsi;
          }
        })
        .anyRequest().authenticated()
    ;
  }


  @Configuration(proxyBeanMethods = false)
  @ConditionalOnWebApplication
  protected static class SecurityConfiguration {

    private final Logger log = LoggerFactory.getLogger(ApiSecurityConfiguration.class);
    private final ApiSecurityProperties securityProperties;

    public SecurityConfiguration(
        ApiSecurityProperties securityProperties) {
      this.securityProperties = securityProperties;
    }

    @Bean
    public ApiSecurityErrorHandler securityOAuth2ErrorHandler(MessageSource messageSource,
        @Autowired(required = false) HttpServletRequest request) {
      return new ApiSecurityErrorHandler(messageSource, request);
    }

    @Bean
    public ApiTokenBuild apiTokenBuild() {
      return new ApiTokenBuild(securityProperties);
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

    @Bean
    public URLFilterInvocationSecurityMetadataSource securityMetadataSource(
        IResourceService resourceService,
        RequestMappingHandlerMapping requestMappingHandlerMapping) {
      return new URLFilterInvocationSecurityMetadataSource(resourceService,
          requestMappingHandlerMapping, securityProperties);
    }

    @ConditionalOnMissingBean
    @Bean
    public AccessDecisionManager accessDecisionManager() {
      return new AccessDecisionManager() {
        @Override
        public void decide(Authentication authentication, Object object,
            Collection<ConfigAttribute> configAttributes) {
          Collection<? extends GrantedAuthority> authorities = authentication.getAuthorities();

          if (log.isDebugEnabled()) {
            log.debug("权限检查，当前用户权限：{}，当前资源需要以下权限之一：{}",
                StringUtils.collectionToCommaDelimitedString(authorities),
                StringUtils.collectionToCommaDelimitedString(configAttributes.stream().map(
                    (Function<ConfigAttribute, Object>) ConfigAttribute::getAttribute).collect(
                    Collectors.toList())));
          }

          for (ConfigAttribute configAttribute : configAttributes) {//需要的权限，有任意其中一个即可
            if (contains(authorities, configAttribute)) {
              return;
            }
          }
          log.info("权限检查，当前用户权限：{}，当前资源需要以下权限之一：{}",
              StringUtils.collectionToCommaDelimitedString(authorities),
              StringUtils.collectionToCommaDelimitedString(configAttributes.stream().map(
                  (Function<ConfigAttribute, Object>) ConfigAttribute::getAttribute).collect(
                  Collectors.toList())));
          throw new AccessDeniedException("无权访问");
        }

        private boolean contains(Collection<? extends GrantedAuthority> authorities,
            ConfigAttribute attribute) {
          String attributeAttribute = attribute.getAttribute();
          for (GrantedAuthority authority : authorities) {
            if (attributeAttribute.equals(authority.getAuthority())) {
              return true;
            }
          }
          return false;
        }

        @Override
        public boolean supports(ConfigAttribute attribute) {
          return true;
        }

        @Override
        public boolean supports(Class<?> clazz) {
          return true;
        }

      };
    }
  }

}