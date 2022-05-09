package top.bettercode.simpleframework.security.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Collection;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.AutoConfigureOrder;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
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
import org.springframework.security.web.authentication.logout.LogoutFilter;
import org.springframework.util.StringUtils;
import top.bettercode.simpleframework.config.CorsProperties;
import top.bettercode.simpleframework.config.SummerWebProperties;
import top.bettercode.simpleframework.security.ApiTokenEndpointFilter;
import top.bettercode.simpleframework.security.ApiTokenService;
import top.bettercode.simpleframework.security.IResourceService;
import top.bettercode.simpleframework.security.IRevokeTokenService;
import top.bettercode.simpleframework.security.URLFilterInvocationSecurityMetadataSource;
import top.bettercode.simpleframework.security.UserDetailsAuthenticationProvider;
import top.bettercode.simpleframework.security.authorization.ApiAuthorizationService;

@Configuration(proxyBeanMethods = false)
@EnableWebSecurity
@AutoConfigureOrder(Ordered.HIGHEST_PRECEDENCE + 10)
@ConditionalOnWebApplication
@ConditionalOnProperty(prefix = "summer.security", name = "enabled", matchIfMissing = true)
@EnableConfigurationProperties({ApiSecurityProperties.class, CorsProperties.class})
public class SecurityConfiguration extends WebSecurityConfigurerAdapter {

  private final ApiSecurityProperties securityProperties;
  private final CorsProperties corsProperties;
  private final URLFilterInvocationSecurityMetadataSource securityMetadataSource;
  private final AccessDecisionManager accessDecisionManager;
  private final ApiTokenService apiTokenService;
  private final IRevokeTokenService revokeTokenService;
  private final SummerWebProperties summerWebProperties;
  private final ObjectMapper objectMapper;
  private final ApiAuthorizationService apiAuthorizationService;

  public SecurityConfiguration(
      ApiSecurityProperties securityProperties,
      CorsProperties corsProperties,
      URLFilterInvocationSecurityMetadataSource securityMetadataSource,
      AccessDecisionManager accessDecisionManager,
      ApiTokenService apiTokenService,
      @Autowired(required = false) IRevokeTokenService revokeTokenService,
      SummerWebProperties summerWebProperties,
      ObjectMapper objectMapper,
      ApiAuthorizationService apiAuthorizationService) {
    this.securityProperties = securityProperties;
    this.corsProperties = corsProperties;
    this.securityMetadataSource = securityMetadataSource;
    this.accessDecisionManager = accessDecisionManager;
    this.apiTokenService = apiTokenService;
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
        authenticationManagerBean(), apiAuthorizationService, apiTokenService,
        summerWebProperties, revokeTokenService, securityProperties, objectMapper);

    http.authenticationProvider(new UserDetailsAuthenticationProvider());
    http.addFilterBefore(apiTokenEndpointFilter, LogoutFilter.class);

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
  protected static class AccessSecurityConfiguration {

    private final Logger log = LoggerFactory.getLogger(
        top.bettercode.simpleframework.security.config.SecurityConfiguration.class);


    @ConditionalOnMissingBean
    @Bean
    public AccessDecisionManager accessDecisionManager(IResourceService resourceService) {
      return new AccessDecisionManager() {
        @Override
        public void decide(Authentication authentication, Object object,
            Collection<ConfigAttribute> configAttributes) {
          Collection<? extends GrantedAuthority> authorities = authentication.getAuthorities();
          //处理匿名用户权限
          if (configAttributes.size() == 1 && "ROLE_ANONYMOUS".equals(
              configAttributes.iterator().next().getAttribute())
              && resourceService.supportsAnonymous()) {
            return;
          }

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