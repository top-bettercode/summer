package top.bettercode.summer.security.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.AutoConfigureOrder;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.security.authorization.AuthorizationManager;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.intercept.RequestAuthorizationContext;
import org.springframework.security.web.authentication.logout.LogoutFilter;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;
import top.bettercode.summer.security.ApiTokenEndpointFilter;
import top.bettercode.summer.security.ApiTokenService;
import top.bettercode.summer.security.IResourceService;
import top.bettercode.summer.security.IRevokeTokenService;
import top.bettercode.summer.security.RequestMappingAuthorizationManager;
import top.bettercode.summer.web.config.CorsProperties;
import top.bettercode.summer.web.config.SummerWebProperties;

@Configuration(proxyBeanMethods = false)
@EnableWebSecurity
@AutoConfigureOrder(Ordered.HIGHEST_PRECEDENCE + 10)
@ConditionalOnWebApplication
@ConditionalOnProperty(prefix = "summer.security", name = "enabled", matchIfMissing = true)
@EnableConfigurationProperties({CorsProperties.class})
public class SecurityConfiguration {

  private final ApiSecurityProperties securityProperties;
  private final CorsProperties corsProperties;
  private final ApiTokenService apiTokenService;
  private final IRevokeTokenService revokeTokenService;
  private final SummerWebProperties summerWebProperties;
  private final ObjectMapper objectMapper;
  private final PasswordEncoder passwordEncoder;

  public SecurityConfiguration(
      CorsProperties corsProperties,
      ApiSecurityProperties securityProperties, ApiTokenService apiTokenService,
      @Autowired(required = false) IRevokeTokenService revokeTokenService,
      SummerWebProperties summerWebProperties,
      ObjectMapper objectMapper, PasswordEncoder passwordEncoder) {
    this.corsProperties = corsProperties;
    this.securityProperties = securityProperties;
    this.apiTokenService = apiTokenService;
    this.revokeTokenService = revokeTokenService;
    this.summerWebProperties = summerWebProperties;
    this.objectMapper = objectMapper;
    this.passwordEncoder = passwordEncoder;
  }

  @Bean
  public RequestMappingAuthorizationManager authorizationManager(
      IResourceService resourceService,
      RequestMappingHandlerMapping requestMappingHandlerMapping) {
    return new RequestMappingAuthorizationManager(resourceService,
        requestMappingHandlerMapping, securityProperties);
  }


  @Bean
  public SecurityFilterChain configure(HttpSecurity http,
      AuthorizationManager<RequestAuthorizationContext> access) throws Exception {
    ApiSecurityProperties securityProperties = apiTokenService.getSecurityProperties();
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

    ApiTokenEndpointFilter apiTokenEndpointFilter = new ApiTokenEndpointFilter(apiTokenService,
        passwordEncoder, summerWebProperties, revokeTokenService, objectMapper);

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
        .authorizeHttpRequests()
        .anyRequest().access(access)
    ;

    return http.build();
  }


}