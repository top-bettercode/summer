package cn.bestwu.simpleframework.security.resource.config;

import cn.bestwu.simpleframework.security.SecurityProperties;
import cn.bestwu.simpleframework.security.resource.URLFilterInvocationSecurityMetadataSource;
import org.springframework.boot.autoconfigure.AutoConfigureOrder;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.security.access.AccessDecisionManager;
import org.springframework.security.config.annotation.ObjectPostProcessor;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.web.access.intercept.FilterSecurityInterceptor;

@Configuration
@EnableWebSecurity
@AutoConfigureOrder(Ordered.HIGHEST_PRECEDENCE + 10)
@ConditionalOnWebApplication
@EnableConfigurationProperties(SecurityProperties.class)
public class SimpleFrameworkResourceServerConfiguration extends
    WebSecurityConfigurerAdapter {

  private final URLFilterInvocationSecurityMetadataSource securityMetadataSource;
  private final AccessDecisionManager accessDecisionManager;
  private final SecurityProperties securityProperties;

  public SimpleFrameworkResourceServerConfiguration(
      URLFilterInvocationSecurityMetadataSource securityMetadataSource,
      AccessDecisionManager accessDecisionManager,
      SecurityProperties securityProperties) {
    this.securityMetadataSource = securityMetadataSource;
    this.accessDecisionManager = accessDecisionManager;
    this.securityProperties = securityProperties;
  }


  @Override
  public void configure(HttpSecurity http) throws Exception {
    if (securityProperties.getSupportClientCache()) {
      http.headers().cacheControl().disable();
    }
    if (securityProperties.getFrameOptionsDisable()) {
      http.headers().frameOptions().disable();
    }

    http.oauth2ResourceServer().and()
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
}