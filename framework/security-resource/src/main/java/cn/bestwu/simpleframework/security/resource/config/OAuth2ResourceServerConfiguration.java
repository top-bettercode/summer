package cn.bestwu.simpleframework.security.resource.config;

import cn.bestwu.simpleframework.security.exception.CustomWebResponseExceptionTranslator;
import cn.bestwu.simpleframework.security.exception.OAuth2ExceptionJackson2Serializer;
import cn.bestwu.simpleframework.security.exception.SecurityOAuth2ErrorHandler;
import cn.bestwu.simpleframework.web.error.IErrorRespEntityHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.common.exceptions.OAuth2Exception;
import org.springframework.security.oauth2.provider.error.DefaultOAuth2ExceptionRenderer;
import org.springframework.security.oauth2.provider.error.OAuth2AccessDeniedHandler;
import org.springframework.security.oauth2.provider.error.OAuth2AuthenticationEntryPoint;
import org.springframework.security.oauth2.provider.error.WebResponseExceptionTranslator;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerAdapter;

@SuppressWarnings("deprecation")
@ConditionalOnClass(OAuth2Exception.class)
@Configuration
@ConditionalOnWebApplication
public class OAuth2ResourceServerConfiguration {

  private final RequestMappingHandlerAdapter requestMappingHandlerAdapter;
  private WebResponseExceptionTranslator<OAuth2Exception> webResponseExceptionTranslator;
  public final MessageSource messageSource;

  public OAuth2ResourceServerConfiguration(
      RequestMappingHandlerAdapter requestMappingHandlerAdapter,
      @Value("${app.web.ok.enable:true}") boolean okEnable,
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
  public SecurityOAuth2ErrorHandler securityErrorHandler(@Autowired(required = false)
      IErrorRespEntityHandler errorRespEntityHandler) {
    if (errorRespEntityHandler != null) {
      OAuth2ExceptionJackson2Serializer.setErrorRespEntityHandler(errorRespEntityHandler);
    }
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

}