package cn.bestwu.simpleframework.security.resource.config;

import cn.bestwu.simpleframework.security.resource.URLFilterInvocationSecurityMetadataSource;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.AutoConfigureOrder;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.security.access.AccessDecisionManager;
import org.springframework.security.config.annotation.ObjectPostProcessor;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.config.annotation.web.configuration.ResourceServerConfigurerAdapter;
import org.springframework.security.oauth2.config.annotation.web.configurers.ResourceServerSecurityConfigurer;
import org.springframework.security.oauth2.provider.error.OAuth2AccessDeniedHandler;
import org.springframework.security.oauth2.provider.error.OAuth2AuthenticationEntryPoint;
import org.springframework.security.web.access.intercept.FilterSecurityInterceptor;

@ConditionalOnClass(ResourceServerConfigurerAdapter.class)
@Configuration
@AutoConfigureOrder(Ordered.HIGHEST_PRECEDENCE + 10)
@ConditionalOnWebApplication
public class SimpleFrameworkResourceServerConfiguration extends
    ResourceServerConfigurerAdapter {

  private final URLFilterInvocationSecurityMetadataSource securityMetadataSource;
  private final OAuth2AccessDeniedHandler oAuth2AccessDeniedHandler;
  private final OAuth2AuthenticationEntryPoint oAuth2AuthenticationEntryPoint;
  private final AccessDecisionManager accessDecisionManager;
  @Value("${server.client-cache:true}")
  private boolean supportClientCache;
  @Value("${security.cors.enable:false}")
  private boolean enableCors;
  @Value("${security.http.session-creation-policy:STATELESS}")
  private SessionCreationPolicy sessionCreationPolicy;
  @Value("${security.http.frame-options-disable:true}")
  private boolean frameOptionsDisable;

  public SimpleFrameworkResourceServerConfiguration(
      URLFilterInvocationSecurityMetadataSource securityMetadataSource,
      OAuth2AccessDeniedHandler oAuth2AccessDeniedHandler,
      OAuth2AuthenticationEntryPoint oAuth2AuthenticationEntryPoint,
      AccessDecisionManager accessDecisionManager) {
    this.securityMetadataSource = securityMetadataSource;
    this.oAuth2AccessDeniedHandler = oAuth2AccessDeniedHandler;
    this.oAuth2AuthenticationEntryPoint = oAuth2AuthenticationEntryPoint;
    this.accessDecisionManager = accessDecisionManager;
  }


  @Override
  public void configure(ResourceServerSecurityConfigurer resources) {
    resources.stateless(false);
    resources.authenticationEntryPoint(oAuth2AuthenticationEntryPoint);
    resources.accessDeniedHandler(oAuth2AccessDeniedHandler);
  }


  @Override
  public void configure(HttpSecurity http) throws Exception {
    if (supportClientCache) {
      http.headers().cacheControl().disable();
    }
    if (enableCors) {
      http.cors();
    }
    if (frameOptionsDisable) {
      http.headers().frameOptions().disable();
    }

    http
        .sessionManagement().sessionCreationPolicy(sessionCreationPolicy)
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
}