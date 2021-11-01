package top.bettercode.simpleframework.security;

import static java.nio.charset.StandardCharsets.US_ASCII;

import java.time.Instant;
import org.apache.tomcat.util.codec.binary.Base64;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.keygen.BytesKeyGenerator;
import org.springframework.security.crypto.keygen.KeyGenerators;
import top.bettercode.simpleframework.security.authorization.ApiAuthorizationService;
import top.bettercode.simpleframework.security.config.ApiSecurityProperties;

/**
 * @author Peter Wu
 */
public class ApiTokenService {

  private static final BytesKeyGenerator DEFAULT_TOKEN_GENERATOR = KeyGenerators.secureRandom(20);

  private final ApiSecurityProperties apiSecurityProperties;
  private final ApiAuthorizationService apiAuthorizationService;
  private final UserDetailsService userDetailsService;
  private final boolean isScopeUserDetailsService;

  public ApiTokenService(
      ApiSecurityProperties apiSecurityProperties,
      ApiAuthorizationService apiAuthorizationService,
      UserDetailsService userDetailsService) {
    this.apiSecurityProperties = apiSecurityProperties;
    this.apiAuthorizationService = apiAuthorizationService;
    this.userDetailsService = userDetailsService;
    this.isScopeUserDetailsService = userDetailsService instanceof ScopeUserDetailsService;
  }

  public Token createAccessToken() {
    String tokenValue = new String(
        Base64.encodeBase64URLSafe(DEFAULT_TOKEN_GENERATOR.generateKey()), US_ASCII);
    Instant now = Instant.now();
    return new Token(tokenValue, now,
        now.plusSeconds(apiSecurityProperties.getAccessTokenValiditySeconds()));
  }

  public Token createRefreshToken() {
    String tokenValue = new String(
        Base64.encodeBase64URLSafe(DEFAULT_TOKEN_GENERATOR.generateKey()), US_ASCII);
    Instant now = Instant.now();
    return new Token(tokenValue, now,
        now.plusSeconds(apiSecurityProperties.getRefreshTokenValiditySeconds()));
  }

  public ApiToken getApiToken(String scope, String username) {
    return getApiToken(scope, username, apiSecurityProperties.getLoginKickedOut());
  }

  public ApiToken getApiToken(String scope, String username, Boolean loginKickedOut) {
    UserDetails userDetails = getUserDetails(scope, username);
    return getApiToken(scope, userDetails, loginKickedOut);
  }

  public ApiToken getApiToken(String scope, UserDetails userDetails) {
    return getApiToken(scope, userDetails, apiSecurityProperties.getLoginKickedOut());
  }

  public ApiToken getApiToken(String scope, UserDetails userDetails, Boolean loginKickedOut) {
    ApiAuthenticationToken authenticationToken;
    if (loginKickedOut) {
      authenticationToken = new ApiAuthenticationToken(scope, createAccessToken(),
          createRefreshToken(), userDetails);
    } else {
      authenticationToken = apiAuthorizationService.findByScopeAndUsername(scope,
          userDetails.getUsername());
      if (authenticationToken == null || authenticationToken.getRefreshToken().isExpired()) {
        authenticationToken = new ApiAuthenticationToken(scope, createAccessToken(),
            createRefreshToken(), userDetails);
      } else if (authenticationToken.getAccessToken().isExpired()) {
        authenticationToken.setAccessToken(createAccessToken());
        authenticationToken.setUserDetails(userDetails);
      } else {
        authenticationToken.setUserDetails(userDetails);
      }
    }
    apiAuthorizationService.save(authenticationToken);
    return authenticationToken.toApiToken();
  }

  private UserDetails getUserDetails(String scope, String username) {
    UserDetails userDetails;
    if (isScopeUserDetailsService) {
      userDetails = ((ScopeUserDetailsService) userDetailsService).loadUserByScopeAndUsername(
          scope, username);
    } else {
      userDetails = userDetailsService.loadUserByUsername(username);
    }
    return userDetails;
  }

  public void removeApiToken(String scope, String username) {
    apiAuthorizationService.remove(scope, username);
  }


}
