package top.bettercode.simpleframework.security;

import static java.nio.charset.StandardCharsets.US_ASCII;

import java.time.Instant;
import javax.servlet.http.HttpServletRequest;
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
  private final boolean isGrantTypeDetailsService;

  public ApiTokenService(
      ApiSecurityProperties apiSecurityProperties,
      ApiAuthorizationService apiAuthorizationService,
      UserDetailsService userDetailsService) {
    this.apiSecurityProperties = apiSecurityProperties;
    this.apiAuthorizationService = apiAuthorizationService;
    this.userDetailsService = userDetailsService;
    this.isScopeUserDetailsService = userDetailsService instanceof ScopeUserDetailsService;
    this.isGrantTypeDetailsService = userDetailsService instanceof GrantTypeUserDetailsService;
  }

  public Token createAccessToken() {
    String tokenValue = new String(
        Base64.encodeBase64URLSafe(DEFAULT_TOKEN_GENERATOR.generateKey()), US_ASCII);
    if (apiAuthorizationService.findByAccessToken(tokenValue) != null) {
      return createAccessToken();
    }
    Instant now = Instant.now();
    Integer accessTokenValiditySeconds = apiSecurityProperties.getAccessTokenValiditySeconds();
    return new Token(tokenValue, now,
        accessTokenValiditySeconds != null && accessTokenValiditySeconds > 0 ? now.plusSeconds(
            accessTokenValiditySeconds) : null);
  }

  public Token createRefreshToken() {
    String tokenValue = new String(
        Base64.encodeBase64URLSafe(DEFAULT_TOKEN_GENERATOR.generateKey()), US_ASCII);
    if (apiAuthorizationService.findByRefreshToken(tokenValue) != null) {
      return createRefreshToken();
    }
    Instant now = Instant.now();
    Integer refreshTokenValiditySeconds = apiSecurityProperties.getRefreshTokenValiditySeconds();
    return new Token(tokenValue, now,
        refreshTokenValiditySeconds != null && refreshTokenValiditySeconds > 0 ? now.plusSeconds(
            refreshTokenValiditySeconds) : null);
  }

  public ApiToken getApiToken(String scope, String username) {
    return getApiToken(scope, username, apiSecurityProperties.needKickedOut(scope));
  }

  public ApiToken getApiToken(String scope, String username, Boolean loginKickedOut) {
    UserDetails userDetails = getUserDetails(scope, username);
    return getApiToken(scope, userDetails, loginKickedOut);
  }

  public ApiToken getApiToken(String scope, String oldUsername, String newUsername,
      Boolean loginKickedOut) {
    UserDetails oldUserDetails = getUserDetails(scope, oldUsername);
    ApiAuthenticationToken authenticationToken = getApiAuthenticationToken(scope, oldUserDetails,
        loginKickedOut);
    authenticationToken.setUserDetails(getUserDetails(scope, newUsername));
    apiAuthorizationService.save(authenticationToken);
    return authenticationToken.toApiToken();
  }

  public ApiToken getApiToken(String scope, UserDetails userDetails) {
    return getApiToken(scope, userDetails, apiSecurityProperties.needKickedOut(scope));
  }

  public ApiToken getApiToken(String scope, UserDetails userDetails, Boolean loginKickedOut) {
    ApiAuthenticationToken authenticationToken = getApiAuthenticationToken(scope, userDetails,
        loginKickedOut);

    apiAuthorizationService.save(authenticationToken);
    return authenticationToken.toApiToken();
  }

  public ApiAuthenticationToken getApiAuthenticationToken(String scope, UserDetails userDetails,
      Boolean loginKickedOut) {
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
    return authenticationToken;
  }

  public UserDetails getUserDetails(String grantType, HttpServletRequest request) {
    if (isGrantTypeDetailsService) {
      return ((GrantTypeUserDetailsService) userDetailsService).loadUserByGrantTypeAndRequest(
          grantType, request);
    }
    throw new IllegalArgumentException("不支持的grantType类型");
  }

  public UserDetails getUserDetails(String scope, String username) {
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
