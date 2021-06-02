package cn.bestwu.simpleframework.security.server;

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

@Deprecated
@Configuration(proxyBeanMethods = false)
@ConditionalOnWebApplication
public class SecurityServerConfiguration extends GlobalAuthenticationConfigurerAdapter {

  public final MessageSource messageSource;
  private final UserDetailsService userDetailsService;

  public SecurityServerConfiguration(MessageSource messageSource,
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