package cn.bestwu.simpleframework.security.resource.config;

import cn.bestwu.simpleframework.security.exception.CustomWebResponseExceptionTranslator;
import cn.bestwu.simpleframework.security.exception.SecurityOAuth2ErrorHandler;
import cn.bestwu.simpleframework.security.resource.IResourceService;
import cn.bestwu.simpleframework.security.resource.URLFilterInvocationSecurityMetadataSource;
import java.util.Collection;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.access.AccessDecisionManager;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.ConfigAttribute;
import org.springframework.security.config.annotation.ObjectPostProcessor;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.common.exceptions.OAuth2Exception;
import org.springframework.security.oauth2.config.annotation.web.configuration.ResourceServerConfigurerAdapter;
import org.springframework.security.oauth2.config.annotation.web.configurers.ResourceServerSecurityConfigurer;
import org.springframework.security.oauth2.provider.error.DefaultOAuth2ExceptionRenderer;
import org.springframework.security.oauth2.provider.error.OAuth2AccessDeniedHandler;
import org.springframework.security.oauth2.provider.error.OAuth2AuthenticationEntryPoint;
import org.springframework.security.oauth2.provider.error.WebResponseExceptionTranslator;
import org.springframework.security.web.access.intercept.FilterSecurityInterceptor;
import org.springframework.util.ClassUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerAdapter;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

@Configuration
@ConditionalOnWebApplication
public class SecurityResourceServerConfiguration {

  private Logger log = LoggerFactory.getLogger(SecurityResourceServerConfiguration.class);

  @Bean
  public URLFilterInvocationSecurityMetadataSource securityMetadataSource(
      IResourceService apiService,
      RequestMappingHandlerMapping requestMappingHandlerMapping,
      @Value("${security.url-filter.ignored:}") String[] ignored,
      @Value("${security.ignore-logs:true}") boolean ignoreLogs) {
    return new URLFilterInvocationSecurityMetadataSource(apiService,
        requestMappingHandlerMapping, ignored, ignoreLogs);
  }

  @ConditionalOnMissingBean
  @Bean
  public AccessDecisionManager accessDecisionManager() {
    return new AccessDecisionManager() {
      @Override
      public void decide(Authentication authentication, Object object,
          Collection<ConfigAttribute> configAttributes) {
        Collection<? extends GrantedAuthority> authorities = authentication.getAuthorities();

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

  @ConditionalOnClass(OAuth2Exception.class)
  @Configuration
  @ConditionalOnWebApplication
  public class OAuth2ResourceServerConfiguration {

    private final RequestMappingHandlerAdapter requestMappingHandlerAdapter;
    private WebResponseExceptionTranslator<OAuth2Exception> webResponseExceptionTranslator;
    public final MessageSource messageSource;

    public OAuth2ResourceServerConfiguration(
        RequestMappingHandlerAdapter requestMappingHandlerAdapter,
        @Value("${app.web.ok.enable:false}") boolean okEnable,
        @Autowired(required = false) WebResponseExceptionTranslator<OAuth2Exception> webResponseExceptionTranslator,
        MessageSource messageSource) {
      this.requestMappingHandlerAdapter = requestMappingHandlerAdapter;
      this.webResponseExceptionTranslator = webResponseExceptionTranslator;
      this.messageSource = messageSource;
      if (this.webResponseExceptionTranslator == null) {
        this.webResponseExceptionTranslator = new CustomWebResponseExceptionTranslator(okEnable,
            this.messageSource);
      }
    }


    @Bean
    public SecurityOAuth2ErrorHandler securityErrorHandler() {
      return new SecurityOAuth2ErrorHandler();
    }

    @Bean
    public OAuth2AccessDeniedHandler accessDeniedHandler(
        DefaultOAuth2ExceptionRenderer exceptionRenderer) {
      OAuth2AccessDeniedHandler accessDeniedHandler = new OAuth2AccessDeniedHandler();
      accessDeniedHandler.setExceptionTranslator(webResponseExceptionTranslator);
      accessDeniedHandler.setExceptionRenderer(exceptionRenderer);
      return accessDeniedHandler;
    }

    @Bean
    public DefaultOAuth2ExceptionRenderer exceptionRenderer() {
      DefaultOAuth2ExceptionRenderer exceptionRenderer = new DefaultOAuth2ExceptionRenderer();
      exceptionRenderer.setMessageConverters(requestMappingHandlerAdapter.getMessageConverters());
      return exceptionRenderer;
    }

    @Bean
    public OAuth2AuthenticationEntryPoint authenticationEntryPoint(
        DefaultOAuth2ExceptionRenderer exceptionRenderer) {
      OAuth2AuthenticationEntryPoint oAuth2AuthenticationEntryPoint = new OAuth2AuthenticationEntryPoint();
      oAuth2AuthenticationEntryPoint.setExceptionTranslator(webResponseExceptionTranslator);
      oAuth2AuthenticationEntryPoint.setExceptionRenderer(exceptionRenderer);
      return oAuth2AuthenticationEntryPoint;
    }


    @ConditionalOnClass(ResourceServerConfigurerAdapter.class)
    @Configuration
    @ConditionalOnWebApplication
    public class SimpleFrameworkResourceServerConfiguration extends
        ResourceServerConfigurerAdapter {

      @Value("${server.client-cache:true}")
      private boolean supportClientCache;
      @Value("${security.cors.enable:false}")
      private boolean enableCors;
      @Value("${security.http.session-creation-policy:STATELESS}")
      private SessionCreationPolicy sessionCreationPolicy;
      @Value("${security.http.frame-options-disable:true}")
      private boolean frameOptionsDisable;
      private final URLFilterInvocationSecurityMetadataSource securityMetadataSource;
      private final OAuth2AccessDeniedHandler oAuth2AccessDeniedHandler;
      private final OAuth2AuthenticationEntryPoint oAuth2AuthenticationEntryPoint;
      private final AccessDecisionManager accessDecisionManager;

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

        if (ClassUtils.isPresent(
            "cn.bestwu.simpleframework.security.server.config.Oauth2SecurityConfiguration",
            SecurityResourceServerConfiguration.class.getClassLoader())) {
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
              });
        } else {
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
              }).anyRequest().authenticated();
        }
      }
    }
  }

}