package cn.bestwu.simpleframework.security.server.config;

import cn.bestwu.simpleframework.security.server.AccessTokenService;
import cn.bestwu.simpleframework.security.server.IRevokeTokenService;
import cn.bestwu.simpleframework.security.server.RevokeTokenEndpoint;
import cn.bestwu.simpleframework.security.server.exception.SecurityOAuth2ErrorHandler;
import javax.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.authentication.configuration.GlobalAuthenticationConfigurerAdapter;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.oauth2.config.annotation.web.configuration.AuthorizationServerEndpointsConfiguration;
import org.springframework.security.oauth2.provider.ClientDetails;
import org.springframework.security.oauth2.provider.token.AuthorizationServerTokenServices;
import org.springframework.security.oauth2.provider.token.ConsumerTokenServices;
import org.springframework.security.oauth2.provider.token.TokenStore;

@SuppressWarnings("deprecation")
@Configuration
@ConditionalOnWebApplication
public class AuthServerConfiguration extends GlobalAuthenticationConfigurerAdapter {

  public final MessageSource messageSource;
  private final UserDetailsService userDetailsService;


  public AuthServerConfiguration(MessageSource messageSource,
      UserDetailsService userDetailsService) {
    this.messageSource = messageSource;
    this.userDetailsService = userDetailsService;
  }

  /**
   * 自定义UserDetailsService
   *
   * @param auth auth
   * @throws Exception Exception
   */
  @Override
  public void init(AuthenticationManagerBuilder auth) throws Exception {
    auth.userDetailsService(userDetailsService);
  }

//  @Configuration
//  @ConditionalOnWebApplication
//  protected static class OAuth2ExceptionBuilderCustomizer implements
//      Jackson2ObjectMapperBuilderCustomizer {
//
//    @Override
//    public void customize(Jackson2ObjectMapperBuilder jacksonObjectMapperBuilder) {
//      jacksonObjectMapperBuilder
//          .serializerByType(OAuth2Exception.class, new OAuth2ExceptionJackson2Serializer());
//    }
//  }

//  @Bean
//  public WebResponseExceptionTranslator<OAuth2Exception> webResponseExceptionTranslator(
//      @Value("${app.web.ok.enable:true}") boolean okEnable) {
//    CustomWebResponseExceptionTranslator exceptionTranslator = new CustomWebResponseExceptionTranslator(
//        okEnable, messageSource);
//    tokenEndpoint.setProviderExceptionHandler(exceptionTranslator);
//    return exceptionTranslator;
//  }

  @Bean
  public SecurityOAuth2ErrorHandler securityOAuth2ErrorHandler(MessageSource messageSource,
      @Autowired(required = false) HttpServletRequest request) {
    return new SecurityOAuth2ErrorHandler(messageSource, request);
  }

  @Bean
  public RevokeTokenEndpoint revokeTokenEndpoint(
      @Qualifier("consumerTokenServices") ConsumerTokenServices consumerTokenServices,
      @Autowired(required = false) IRevokeTokenService revokeTokenService,
      AuthorizationServerEndpointsConfiguration authorizationServerEndpointsConfigurer) {
    return new RevokeTokenEndpoint(consumerTokenServices, revokeTokenService,
        authorizationServerEndpointsConfigurer);
  }

  @Bean
  public AccessTokenService accessTokenService(ClientDetails clientDetails,
      UserDetailsService userDetailsService,
      AuthorizationServerTokenServices authorizationServerTokenServices,
      @Autowired(required = false) TokenStore tokenStore) {
    return new AccessTokenService(clientDetails, userDetailsService,
        authorizationServerTokenServices, tokenStore);
  }


}