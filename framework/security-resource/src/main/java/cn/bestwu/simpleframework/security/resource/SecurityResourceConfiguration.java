package cn.bestwu.simpleframework.security.resource;

import java.util.Collection;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.AutoConfigureOrder;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
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
import org.springframework.security.config.annotation.web.configurers.oauth2.server.resource.OAuth2ResourceServerConfigurer;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.config.annotation.web.configuration.AuthorizationServerEndpointsConfiguration;
import org.springframework.security.oauth2.server.resource.introspection.OpaqueTokenIntrospector;
import org.springframework.security.web.access.intercept.FilterSecurityInterceptor;
import org.springframework.util.StringUtils;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

@Configuration
@EnableWebSecurity
@AutoConfigureOrder(Ordered.HIGHEST_PRECEDENCE + 10)
@ConditionalOnWebApplication
@EnableConfigurationProperties(SecurityProperties.class)
public class SecurityResourceConfiguration extends WebSecurityConfigurerAdapter {

  private final SecurityProperties securityProperties;
  private final URLFilterInvocationSecurityMetadataSource securityMetadataSource;
  private final AccessDecisionManager accessDecisionManager;

  public SecurityResourceConfiguration(
      SecurityProperties securityProperties,
      URLFilterInvocationSecurityMetadataSource securityMetadataSource,
      AccessDecisionManager accessDecisionManager) {
    this.securityProperties = securityProperties;
    this.securityMetadataSource = securityMetadataSource;
    this.accessDecisionManager = accessDecisionManager;
  }


  @Deprecated
  @ConditionalOnMissingBean(OpaqueTokenIntrospector.class)
  @ConditionalOnBean(AuthorizationServerEndpointsConfiguration.class)
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

    http.oauth2ResourceServer(OAuth2ResourceServerConfigurer::opaqueToken)
        .sessionManagement().sessionCreationPolicy(securityProperties.getSessionCreationPolicy())
        .and()
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


  @Configuration
  @ConditionalOnWebApplication
  protected static class AccessDecisionManagerConfiguration {

    private final Logger log = LoggerFactory.getLogger(SecurityResourceConfiguration.class);
    private final SecurityProperties securityProperties;

    public AccessDecisionManagerConfiguration(
        SecurityProperties securityProperties) {
      this.securityProperties = securityProperties;
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