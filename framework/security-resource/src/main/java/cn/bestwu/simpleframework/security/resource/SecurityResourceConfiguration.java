package cn.bestwu.simpleframework.security.resource;

import cn.bestwu.simpleframework.config.CorsProperties;
import cn.bestwu.simpleframework.security.ClientDetailsProperties;
import cn.bestwu.simpleframework.security.server.SecurityServerConfiguration;
import java.io.IOException;
import java.util.Collection;
import java.util.function.Function;
import java.util.stream.Collectors;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.AutoConfigureOrder;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
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
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.config.annotation.web.configuration.AuthorizationServerEndpointsConfiguration;
import org.springframework.security.oauth2.server.resource.introspection.OpaqueTokenIntrospector;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.security.web.access.intercept.FilterSecurityInterceptor;
import org.springframework.util.StringUtils;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

@Configuration(proxyBeanMethods = false)
@EnableWebSecurity
@AutoConfigureOrder(Ordered.HIGHEST_PRECEDENCE + 10)
@ConditionalOnWebApplication
@EnableConfigurationProperties({SecurityProperties.class, CorsProperties.class})
public class SecurityResourceConfiguration extends WebSecurityConfigurerAdapter {

  private final SecurityProperties securityProperties;
  private final CorsProperties corsProperties;
  private final URLFilterInvocationSecurityMetadataSource securityMetadataSource;
  private final AccessDecisionManager accessDecisionManager;

  public SecurityResourceConfiguration(
      SecurityProperties securityProperties,
      CorsProperties corsProperties,
      URLFilterInvocationSecurityMetadataSource securityMetadataSource,
      AccessDecisionManager accessDecisionManager) {
    this.securityProperties = securityProperties;
    this.corsProperties = corsProperties;
    this.securityMetadataSource = securityMetadataSource;
    this.accessDecisionManager = accessDecisionManager;
  }


  @Deprecated
  @ConditionalOnMissingBean(OpaqueTokenIntrospector.class)
  @ConditionalOnClass(SecurityServerConfiguration.class)
  @Bean
  public OpaqueTokenIntrospector opaqueTokenIntrospector(
      AuthorizationServerEndpointsConfiguration configuration) {
    return new SpringOpaqueTokenIntrospector(configuration);
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

    http.oauth2ResourceServer(config -> {
      config.opaqueToken();
      config.bearerTokenResolver(new MultipleBearerTokenResolver());
    })
        .sessionManagement().sessionCreationPolicy(securityProperties.getSessionCreationPolicy())
        .and().exceptionHandling(config->{
          config.accessDeniedHandler(new AccessDeniedHandler() {
            @Override
            public void handle(HttpServletRequest request, HttpServletResponse response,
                AccessDeniedException accessDeniedException) throws IOException, ServletException {
              throw accessDeniedException;
            }
          });
          config.authenticationEntryPoint(new AuthenticationEntryPoint(){
            @Override
            public void commence(HttpServletRequest request, HttpServletResponse response,
                AuthenticationException authException) throws IOException, ServletException {
              throw authException;
            }
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
  @EnableConfigurationProperties(ClientDetailsProperties.class)
  @ConditionalOnWebApplication
  protected static class AccessDecisionManagerConfiguration implements WebMvcConfigurer {

    private final Logger log = LoggerFactory.getLogger(SecurityResourceConfiguration.class);
    private final SecurityProperties securityProperties;
    private final ClientDetailsProperties clientDetailsProperties;

    public AccessDecisionManagerConfiguration(
        SecurityProperties securityProperties,
        ClientDetailsProperties clientDetailsProperties) {
      this.securityProperties = securityProperties;
      this.clientDetailsProperties = clientDetailsProperties;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
      registry.addInterceptor(new ClientAuthorizeHandlerInterceptor(clientDetailsProperties));
    }


    @Bean
    @RefreshScope
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