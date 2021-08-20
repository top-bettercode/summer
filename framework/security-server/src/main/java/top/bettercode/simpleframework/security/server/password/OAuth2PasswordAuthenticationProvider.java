package top.bettercode.simpleframework.security.server.password;

import java.util.LinkedHashSet;
import java.util.Set;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.OAuth2AccessToken;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2ErrorCodes;
import org.springframework.security.oauth2.core.OAuth2TokenType;
import org.springframework.security.oauth2.jwt.JoseHeader;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.server.authorization.JwtEncodingContext;
import org.springframework.security.oauth2.server.authorization.OAuth2Authorization;
import org.springframework.security.oauth2.server.authorization.OAuth2AuthorizationService;
import org.springframework.security.oauth2.server.authorization.OAuth2TokenCustomizer;
import org.springframework.security.oauth2.server.authorization.authentication.OAuth2AccessTokenAuthenticationToken;
import org.springframework.security.oauth2.server.authorization.authentication.OAuth2ClientAuthenticationToken;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClient;
import org.springframework.security.oauth2.server.authorization.config.ProviderSettings;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;


public final class OAuth2PasswordAuthenticationProvider implements AuthenticationProvider {

  private final OAuth2AuthorizationService authorizationService;
  private final JwtEncoder jwtEncoder;
  private final PasswordEncoder passwordEncoder;
  private final UserDetailsService userDetailsService;
  private OAuth2TokenCustomizer<JwtEncodingContext> jwtCustomizer = (context) -> {
  };
  private ProviderSettings providerSettings;


  public OAuth2PasswordAuthenticationProvider(OAuth2AuthorizationService authorizationService,
      JwtEncoder jwtEncoder,
      PasswordEncoder passwordEncoder,
      UserDetailsService userDetailsService) {
    this.passwordEncoder = passwordEncoder;
    this.userDetailsService = userDetailsService;
    Assert.notNull(authorizationService, "authorizationService cannot be null");
    Assert.notNull(jwtEncoder, "jwtEncoder cannot be null");
    this.authorizationService = authorizationService;
    this.jwtEncoder = jwtEncoder;
  }


  public void setJwtCustomizer(OAuth2TokenCustomizer<JwtEncodingContext> jwtCustomizer) {
    Assert.notNull(jwtCustomizer, "jwtCustomizer cannot be null");
    this.jwtCustomizer = jwtCustomizer;
  }

  @Autowired(required = false)
  protected void setProviderSettings(ProviderSettings providerSettings) {
    this.providerSettings = providerSettings;
  }

  @Override
  public Authentication authenticate(Authentication authentication) throws AuthenticationException {
    OAuth2PasswordAuthenticationToken auth2PasswordAuthenticationToken =
        (OAuth2PasswordAuthenticationToken) authentication;

    OAuth2ClientAuthenticationToken clientPrincipal =
        getAuthenticatedClientElseThrowInvalidClient(auth2PasswordAuthenticationToken);
    RegisteredClient registeredClient = clientPrincipal.getRegisteredClient();

    if (!registeredClient.getAuthorizationGrantTypes()
        .contains(AuthorizationGrantType.PASSWORD)) {
      throw new OAuth2AuthenticationException(OAuth2ErrorCodes.UNAUTHORIZED_CLIENT);
    }

    Set<String> authorizedScopes = registeredClient.getScopes();    // Default to configured scopes
    if (!CollectionUtils.isEmpty(auth2PasswordAuthenticationToken.getScopes())) {
      for (String requestedScope : auth2PasswordAuthenticationToken.getScopes()) {
        if (!registeredClient.getScopes().contains(requestedScope)) {
          throw new OAuth2AuthenticationException(OAuth2ErrorCodes.INVALID_SCOPE);
        }
      }
      authorizedScopes = new LinkedHashSet<>(auth2PasswordAuthenticationToken.getScopes());
    }

    String issuer = this.providerSettings != null ? this.providerSettings.getIssuer() : null;

    UserDetails userDetails = userDetailsService.loadUserByUsername(
        auth2PasswordAuthenticationToken.getUsername());
    Assert.isTrue(passwordEncoder.matches(auth2PasswordAuthenticationToken.getPassword(),
        userDetails.getPassword()), "用户名或密码不正确");

    UserDetailsAuthenticationToken userDetailsPrincipal = new UserDetailsAuthenticationToken(
        userDetails);

    JoseHeader.Builder headersBuilder = JwtUtils.headers();
    JwtClaimsSet.Builder claimsBuilder = JwtUtils.accessTokenClaims(
        registeredClient, issuer, userDetailsPrincipal.getName(), authorizedScopes);

    // @formatter:off
    JwtEncodingContext context = JwtEncodingContext.with(headersBuilder, claimsBuilder)
        .registeredClient(registeredClient)
        .principal(userDetailsPrincipal)
        .authorizedScopes(authorizedScopes)
        .tokenType(OAuth2TokenType.ACCESS_TOKEN)
        .authorizationGrantType(AuthorizationGrantType.PASSWORD)
        .authorizationGrant(auth2PasswordAuthenticationToken)
        .build();
    // @formatter:on

    this.jwtCustomizer.customize(context);

    JoseHeader headers = context.getHeaders().build();
    JwtClaimsSet claims = context.getClaims().build();
    Jwt jwtAccessToken = this.jwtEncoder.encode(headers, claims);

    OAuth2AccessToken accessToken = new OAuth2AccessToken(OAuth2AccessToken.TokenType.BEARER,
        jwtAccessToken.getTokenValue(), jwtAccessToken.getIssuedAt(),
        jwtAccessToken.getExpiresAt(), authorizedScopes);

    // @formatter:off
    OAuth2Authorization authorization = OAuth2Authorization.withRegisteredClient(registeredClient)
        .principalName(userDetailsPrincipal.getName())
        .authorizationGrantType(AuthorizationGrantType.PASSWORD)
        .token(accessToken,
            (metadata) ->
                metadata.put(OAuth2Authorization.Token.CLAIMS_METADATA_NAME,
                    jwtAccessToken.getClaims()))
        .attribute(OAuth2Authorization.AUTHORIZED_SCOPE_ATTRIBUTE_NAME, authorizedScopes)
        .build();
    // @formatter:on

    this.authorizationService.save(authorization);

    return new OAuth2AccessTokenAuthenticationToken(registeredClient,
        userDetailsPrincipal, accessToken);
  }

  static OAuth2ClientAuthenticationToken getAuthenticatedClientElseThrowInvalidClient(
      Authentication authentication) {
    OAuth2ClientAuthenticationToken clientPrincipal = null;
    if (OAuth2ClientAuthenticationToken.class.isAssignableFrom(
        authentication.getPrincipal().getClass())) {
      clientPrincipal = (OAuth2ClientAuthenticationToken) authentication.getPrincipal();
    }
    if (clientPrincipal != null && clientPrincipal.isAuthenticated()) {
      return clientPrincipal;
    }
    throw new OAuth2AuthenticationException(OAuth2ErrorCodes.INVALID_CLIENT);
  }

  @Override
  public boolean supports(Class<?> authentication) {
    return OAuth2PasswordAuthenticationToken.class.isAssignableFrom(authentication);
  }
}
