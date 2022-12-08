package top.bettercode.summer.security;

import java.time.Instant;
import javax.servlet.http.HttpServletRequest;
import org.apache.tomcat.util.codec.binary.Base64;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.keygen.BytesKeyGenerator;
import org.springframework.security.crypto.keygen.KeyGenerators;
import top.bettercode.summer.security.config.ApiSecurityProperties;
import top.bettercode.summer.security.repository.ApiTokenRepository;
import top.bettercode.summer.security.token.ApiAccessToken;
import top.bettercode.summer.security.token.ApiToken;
import top.bettercode.summer.security.token.InstantAt;
import top.bettercode.summer.security.token.Token;
import top.bettercode.summer.security.userdetails.CheckedUserDetailsService;
import top.bettercode.summer.security.userdetails.GrantTypeUserDetailsService;
import top.bettercode.summer.security.userdetails.NeedKickedOutUserDetailsService;
import top.bettercode.summer.security.userdetails.ScopeUserDetailsService;

/**
 * @author Peter Wu
 */
public class ApiTokenService {

  private static final BytesKeyGenerator DEFAULT_TOKEN_GENERATOR = KeyGenerators.secureRandom(20);

  private final ApiSecurityProperties securityProperties;
  private final ApiTokenRepository apiTokenRepository;
  private final UserDetailsService userDetailsService;
  private final boolean isScopeUserDetailsService;
  private final boolean isGrantTypeDetailsService;
  private final boolean isCheckedDetailsService;
  private final boolean isNeedKickedDetailsService;

  public ApiTokenService(
      ApiSecurityProperties securityProperties,
      ApiTokenRepository apiTokenRepository,
      UserDetailsService userDetailsService) {
    this.securityProperties = securityProperties;
    this.apiTokenRepository = apiTokenRepository;
    this.userDetailsService = userDetailsService;
    this.isScopeUserDetailsService = userDetailsService instanceof ScopeUserDetailsService;
    this.isGrantTypeDetailsService = userDetailsService instanceof GrantTypeUserDetailsService;
    this.isCheckedDetailsService = userDetailsService instanceof CheckedUserDetailsService;
    this.isNeedKickedDetailsService = userDetailsService instanceof NeedKickedOutUserDetailsService;
  }

  public ApiSecurityProperties getSecurityProperties() {
    return securityProperties;
  }

  public ApiTokenRepository getApiTokenRepository() {
    return apiTokenRepository;
  }

  public Token createAccessToken() {
    String tokenValue = Base64.encodeBase64URLSafeString(DEFAULT_TOKEN_GENERATOR.generateKey());
    if (apiTokenRepository.findByAccessToken(tokenValue) != null) {
      return createAccessToken();
    }
    Instant now = Instant.now();
    Integer accessTokenValiditySeconds = securityProperties.getAccessTokenValiditySeconds();
    return new Token(tokenValue, now,
        accessTokenValiditySeconds != null && accessTokenValiditySeconds > 0 ? now.plusSeconds(
            accessTokenValiditySeconds) : null);
  }

  public Token createRefreshToken() {
    String tokenValue = Base64.encodeBase64URLSafeString(DEFAULT_TOKEN_GENERATOR.generateKey());
    if (apiTokenRepository.findByRefreshToken(tokenValue) != null) {
      return createRefreshToken();
    }
    Instant now = Instant.now();
    Integer refreshTokenValiditySeconds = securityProperties.getRefreshTokenValiditySeconds();
    return new Token(tokenValue, now,
        refreshTokenValiditySeconds != null && refreshTokenValiditySeconds > 0 ? now.plusSeconds(
            refreshTokenValiditySeconds) : null);
  }

  public InstantAt createUserDetailsInstantAt() {
    Instant now = Instant.now();
    Integer userDetailsValiditySeconds = securityProperties.getUserDetailsValiditySeconds();
    return new InstantAt(now,
        userDetailsValiditySeconds != null && userDetailsValiditySeconds > 0 ? now.plusSeconds(
            userDetailsValiditySeconds) : null);
  }

  public ApiAccessToken getApiAccessToken(String scope, String username) {
    UserDetails userDetails = getUserDetails(scope, username);
    return getApiAccessToken(scope, userDetails, needKickedOut(scope, userDetails));
  }

  public ApiAccessToken getApiAccessToken(String scope, String username, boolean loginKickedOut) {
    UserDetails userDetails = getUserDetails(scope, username);
    return getApiAccessToken(scope, userDetails, loginKickedOut);
  }

  public ApiAccessToken getApiAccessToken(String scope, String oldUsername, String newUsername,
      boolean loginKickedOut) {
    UserDetails oldUserDetails = getUserDetails(scope, oldUsername);
    ApiToken apiToken = getApiToken(scope, oldUserDetails, loginKickedOut);
    apiToken.setUserDetailsInstantAt(createUserDetailsInstantAt());
    apiToken.setUserDetails(getUserDetails(scope, newUsername));
    apiTokenRepository.save(apiToken);
    return apiToken.toApiToken();
  }

  public ApiAccessToken getApiAccessToken(String scope, UserDetails userDetails) {
    return getApiAccessToken(scope, userDetails, needKickedOut(scope, userDetails));
  }

  public ApiAccessToken getApiAccessToken(String scope, UserDetails userDetails,
      boolean loginKickedOut) {
    ApiToken authenticationToken = getApiToken(scope, userDetails, loginKickedOut);
    apiTokenRepository.save(authenticationToken);
    return authenticationToken.toApiToken();
  }


  public ApiToken getApiToken(String scope, UserDetails userDetails) {
    return getApiToken(scope, userDetails, needKickedOut(scope, userDetails));
  }


  public ApiToken getApiToken(String scope, UserDetails userDetails, boolean loginKickedOut) {
    ApiToken apiToken;
    if (loginKickedOut) {
      apiToken = new ApiToken(scope, createAccessToken(),
          createRefreshToken(), createUserDetailsInstantAt(), userDetails);
    } else {
      apiToken = apiTokenRepository.findByScopeAndUsername(scope,
          userDetails.getUsername());
      if (apiToken == null || apiToken.getRefreshToken().isExpired()) {
        apiToken = new ApiToken(scope, createAccessToken(),
            createRefreshToken(), createUserDetailsInstantAt(), userDetails);
      } else if (apiToken.getAccessToken().isExpired()) {
        apiToken.setAccessToken(createAccessToken());
        apiToken.setUserDetailsInstantAt(createUserDetailsInstantAt());
        apiToken.setUserDetails(userDetails);
      } else {
        apiToken.setUserDetailsInstantAt(createUserDetailsInstantAt());
        apiToken.setUserDetails(userDetails);
      }
    }
    return apiToken;
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
    apiTokenRepository.remove(scope, username);
  }

  public void validate(UserDetails userDetails) {
    if (isCheckedDetailsService) {
      ((CheckedUserDetailsService) userDetailsService).validate(userDetails);
    }
  }

  private boolean needKickedOut(String scope, UserDetails userDetails) {
    if (isNeedKickedDetailsService) {
      return ((NeedKickedOutUserDetailsService) userDetailsService).needKickedOut(scope,
          userDetails);
    } else {
      return securityProperties.needKickedOut(scope);
    }
  }

}
