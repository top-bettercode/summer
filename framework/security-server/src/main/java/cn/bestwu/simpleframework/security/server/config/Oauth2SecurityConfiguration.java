package cn.bestwu.simpleframework.security.server.config;

import cn.bestwu.simpleframework.security.exception.CustomWebResponseExceptionTranslator;
import cn.bestwu.simpleframework.security.server.AccessTokenService;
import cn.bestwu.simpleframework.security.server.IRevokeTokenService;
import cn.bestwu.simpleframework.security.server.RevokeTokenEndpoint;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.oauth2.common.exceptions.OAuth2Exception;
import org.springframework.security.oauth2.config.annotation.web.configuration.AuthorizationServerEndpointsConfiguration;
import org.springframework.security.oauth2.provider.ClientDetails;
import org.springframework.security.oauth2.provider.endpoint.TokenEndpoint;
import org.springframework.security.oauth2.provider.error.WebResponseExceptionTranslator;
import org.springframework.security.oauth2.provider.token.AuthorizationServerTokenServices;
import org.springframework.security.oauth2.provider.token.ConsumerTokenServices;

@ConditionalOnClass(OAuth2Exception.class)
@Configuration
@ConditionalOnWebApplication
public class Oauth2SecurityConfiguration {

  public final MessageSource messageSource;
  private final TokenEndpoint tokenEndpoint;

  public Oauth2SecurityConfiguration(MessageSource messageSource,
      TokenEndpoint tokenEndpoint) {
    this.messageSource = messageSource;
    this.tokenEndpoint = tokenEndpoint;
  }

  @Bean
  public WebResponseExceptionTranslator<OAuth2Exception> webResponseExceptionTranslator(@Value("${app.web.ok.enable:false}") boolean okEnable) {
    CustomWebResponseExceptionTranslator exceptionTranslator = new CustomWebResponseExceptionTranslator(
        okEnable, messageSource);
    tokenEndpoint.setProviderExceptionHandler(exceptionTranslator);
    return exceptionTranslator;
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
      AuthorizationServerTokenServices authorizationServerTokenServices) {
    return new AccessTokenService(clientDetails, userDetailsService,
        authorizationServerTokenServices);
  }

}