package top.bettercode.simpleframework.security;

import static java.nio.charset.StandardCharsets.US_ASCII;

import java.time.Instant;
import org.apache.tomcat.util.codec.binary.Base64;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.keygen.BytesKeyGenerator;
import org.springframework.security.crypto.keygen.KeyGenerators;
import top.bettercode.simpleframework.security.authorization.ApiAuthorizationService;
import top.bettercode.simpleframework.security.config.ApiSecurityProperties;

/**
 * @author Peter Wu
 */
public class TokenBuild {

  private static final BytesKeyGenerator DEFAULT_TOKEN_GENERATOR = KeyGenerators.secureRandom(20);

  private final ApiSecurityProperties apiSecurityProperties;
  private final ApiAuthorizationService apiAuthorizationService;
  private final ScopeUserDetailsService userDetailsService;

  public TokenBuild(
      ApiSecurityProperties apiSecurityProperties,
      ApiAuthorizationService apiAuthorizationService,
      ScopeUserDetailsService userDetailsService) {
    this.apiSecurityProperties = apiSecurityProperties;
    this.apiAuthorizationService = apiAuthorizationService;
    this.userDetailsService = userDetailsService;
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
    ApiAuthenticationToken authenticationToken = apiAuthorizationService.findByScopeAndUsername(
        scope, username);
    if (authenticationToken == null) {
      UserDetails userDetails = userDetailsService.loadUserByScopeAndUsername(scope, username);
      authenticationToken = new ApiAuthenticationToken(scope, createAccessToken(),
          createRefreshToken(), userDetails);
      apiAuthorizationService.save(authenticationToken);
    }
    return authenticationToken.toApiToken();
  }

  public void removeApiToken(String scope, String username) {
    apiAuthorizationService.remove(scope, username);
  }


}
