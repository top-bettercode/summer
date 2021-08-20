package top.bettercode.simpleframework.security.server;

import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jose.jwk.source.JWKSource;
import com.nimbusds.jose.proc.SecurityContext;
import java.time.Instant;
import java.util.Arrays;
import java.util.UUID;
import javax.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.autoconfigure.jackson.Jackson2ObjectMapperBuilderCustomizer;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.oauth2.server.authorization.OAuth2AuthorizationServerConfigurer;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.ClientAuthenticationMethod;
import org.springframework.security.oauth2.core.endpoint.OAuth2AccessTokenResponse;
import org.springframework.security.oauth2.server.authorization.InMemoryOAuth2AuthorizationConsentService;
import org.springframework.security.oauth2.server.authorization.InMemoryOAuth2AuthorizationService;
import org.springframework.security.oauth2.server.authorization.OAuth2AuthorizationConsentService;
import org.springframework.security.oauth2.server.authorization.OAuth2AuthorizationService;
import org.springframework.security.oauth2.server.authorization.client.InMemoryRegisteredClientRepository;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClient;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClient.Builder;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClientRepository;
import org.springframework.security.oauth2.server.authorization.config.ClientSettings;
import org.springframework.security.oauth2.server.authorization.config.ProviderSettings;
import org.springframework.security.oauth2.server.authorization.web.authentication.DelegatingAuthenticationConverter;
import org.springframework.security.oauth2.server.authorization.web.authentication.OAuth2AuthorizationCodeAuthenticationConverter;
import org.springframework.security.oauth2.server.authorization.web.authentication.OAuth2ClientCredentialsAuthenticationConverter;
import org.springframework.security.oauth2.server.authorization.web.authentication.OAuth2RefreshTokenAuthenticationConverter;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.util.matcher.RequestMatcher;
import top.bettercode.simpleframework.config.WebProperties;
import top.bettercode.simpleframework.security.ClientDetailsProperties;
import top.bettercode.simpleframework.security.server.jose.Jwks;
import top.bettercode.simpleframework.security.server.password.OAuth2ConfigurerUtils;
import top.bettercode.simpleframework.security.server.password.OAuth2PasswordAuthenticationConverter;
import top.bettercode.simpleframework.security.server.password.OAuth2PasswordAuthenticationProvider;

@Configuration(proxyBeanMethods = false)
@ConditionalOnWebApplication
@EnableConfigurationProperties(ClientDetailsProperties.class)
public class AuthorizationServerConfiguration {

  private final ClientDetailsProperties clientDetailsProperties;

  public AuthorizationServerConfiguration(
      ClientDetailsProperties clientDetailsProperties) {
    this.clientDetailsProperties = clientDetailsProperties;
  }

  @Configuration(proxyBeanMethods = false)
  @ConditionalOnWebApplication
  protected static class SecurityObjectMapperBuilderCustomizer implements
      Jackson2ObjectMapperBuilderCustomizer {
    private final WebProperties webProperties;
    private final HttpServletRequest request;

    public SecurityObjectMapperBuilderCustomizer(
        WebProperties webProperties,@Autowired(required = false) HttpServletRequest request) {
      this.webProperties = webProperties;
      this.request = request;
    }

    @Override
    public void customize(Jackson2ObjectMapperBuilder jacksonObjectMapperBuilder) {
      jacksonObjectMapperBuilder.serializerByType(OAuth2AccessTokenResponse.class,
          new OAuth2AccessTokenResponseJackson2Serializer(webProperties,request));

    }
  }


  @Bean
  @Order(Ordered.HIGHEST_PRECEDENCE)
  public SecurityFilterChain authorizationServerSecurityFilterChain(HttpSecurity http,
      @Value("${summer.security.cors.enable:true}") boolean corsEnable,
      UserDetailsService userDetailsService,
      PasswordEncoder passwordEncoder,
      @Autowired(required = false) IRevokeTokenService revokeTokenService)
      throws Exception {
//    if (corsEnable) {
//      http.cors();
//    }
//    OAuth2AuthorizationServerConfiguration.applyDefaultSecurity(http);

    OAuth2AuthorizationServerConfigurer<HttpSecurity> authorizationServerConfigurer = new OAuth2AuthorizationServerConfigurer<>();

    OAuth2PasswordAuthenticationProvider oAuth2PasswordAuthenticationProvider = new OAuth2PasswordAuthenticationProvider(
        OAuth2ConfigurerUtils.getAuthorizationService(http),
        OAuth2ConfigurerUtils.getJwtEncoder(http), passwordEncoder, userDetailsService);

    oAuth2PasswordAuthenticationProvider.setJwtCustomizer(context -> context.getClaims().expiresAt(
        Instant.now().plusSeconds(clientDetailsProperties.getAccessTokenValiditySeconds())));

    RequestMatcher endpointsMatcher = authorizationServerConfigurer
        .getEndpointsMatcher();
    authorizationServerConfigurer.tokenEndpoint(o -> {
      o.accessTokenRequestConverter(new DelegatingAuthenticationConverter(
          Arrays.asList(
              new OAuth2AuthorizationCodeAuthenticationConverter(),
              new OAuth2PasswordAuthenticationConverter(),
              new OAuth2RefreshTokenAuthenticationConverter(),
              new OAuth2ClientCredentialsAuthenticationConverter())));
      o.authenticationProvider(oAuth2PasswordAuthenticationProvider);
    })
    ;
    http
        .requestMatcher(endpointsMatcher)
        .authorizeRequests(authorizeRequests ->
            authorizeRequests.anyRequest().authenticated()
        )
        .csrf(csrf -> csrf.ignoringRequestMatchers(endpointsMatcher))
        .apply(authorizationServerConfigurer);

//    BeforeOAuth2TokenRevocationEndpointFilter tokenRevocationEndpointFilter =
//        new BeforeOAuth2TokenRevocationEndpointFilter(oauth2AuthorizationService,
//            revokeTokenService,
//            authenticationManager);
//    http.addFilterBefore(tokenRevocationEndpointFilter, OAuth2TokenRevocationEndpointFilter.class);

    return http.build();
  }

  @Bean
  public OAuth2ErrorHandler oauth2ErrorHandler(MessageSource messageSource,
      @Autowired(required = false) HttpServletRequest request) {
    return new OAuth2ErrorHandler(messageSource, request);
  }

//  @Bean
//  public AccessTokenService accessTokenService(UserDetailsService userDetailsService,
//      OAuth2AuthorizationService oauth2AuthorizationService, AuthenticationManager authenticationManager,
//      ClientDetailsProperties clientDetailsProperties) {
//    return new AccessTokenService(userDetailsService, oauth2AuthorizationService,
//        authenticationManager, clientDetailsProperties);
//  }

  @Bean
  public RegisteredClientRepository registeredClientRepository(
  ) {
    Builder builder = RegisteredClient.withId(UUID.randomUUID().toString())
        .clientId(clientDetailsProperties.getClientId())
        .clientSecret(clientDetailsProperties.getClientSecret())
        .clientAuthenticationMethod(ClientAuthenticationMethod.CLIENT_SECRET_BASIC);
    clientDetailsProperties.getAuthorizedGrantTypes().stream().map(AuthorizationGrantType::new)
        .forEach(
            builder::authorizationGrantType);
    clientDetailsProperties.getScope().forEach(builder::scope);

    RegisteredClient registeredClient = builder.clientSettings(
            ClientSettings.builder().requireAuthorizationConsent(false).build())
        .build();

    return new InMemoryRegisteredClientRepository(registeredClient);
  }

  @ConditionalOnMissingBean(OAuth2AuthorizationService.class)
  @Bean
  public OAuth2AuthorizationService authorizationService(
      RegisteredClientRepository registeredClientRepository) {
    return new InMemoryOAuth2AuthorizationService();
  }

  @ConditionalOnMissingBean(OAuth2AuthorizationConsentService.class)
  @Bean
  public OAuth2AuthorizationConsentService authorizationConsentService(
      RegisteredClientRepository registeredClientRepository) {
    return new InMemoryOAuth2AuthorizationConsentService();
  }

  @Bean
  public ProviderSettings providerSettings() {
    return ProviderSettings.builder().tokenEndpoint("/oauth/token").build();
  }


  @Bean
  public JWKSource<SecurityContext> jwkSource() {
    RSAKey rsaKey = Jwks.generateRsa();
    JWKSet jwkSet = new JWKSet(rsaKey);
    return (jwkSelector, securityContext) -> jwkSelector.select(jwkSet);
  }

}
