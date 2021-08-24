package top.bettercode.simpleframework.security.authorization;

import java.util.Map;
import top.bettercode.simpleframework.security.ApiAuthenticationToken;

/**
 * @author Peter Wu
 */
public class InMemoryApiAuthorizationService implements ApiAuthorizationService {

  private final Map<String, ApiAuthenticationToken> tokenMap;
  private final Map<String, String> accessTokenMap;
  private final Map<String, String> refreshTokenMap;

  public InMemoryApiAuthorizationService(
      Map<String, ApiAuthenticationToken> tokenMap,
      Map<String, String> accessTokenMap,
      Map<String, String> refreshTokenMap) {
    this.tokenMap = tokenMap;
    this.accessTokenMap = accessTokenMap;
    this.refreshTokenMap = refreshTokenMap;
  }

  @Override
  public void save(ApiAuthenticationToken authorization) {
    String scope = authorization.getScope();
    String username = authorization.getUsername();
    String id = scope + ":" + username;
    remove(scope, username);
    accessTokenMap.put(authorization.getAccessToken().getTokenValue(), id);
    refreshTokenMap.put(authorization.getRefreshToken().getTokenValue(), id);
    tokenMap.put(id, authorization);
  }

  @Override
  public void remove(ApiAuthenticationToken authorization) {
    String scope = authorization.getScope();
    String username = authorization.getUsername();
    String id = scope + ":" + username;
    accessTokenMap.remove(authorization.getAccessToken().getTokenValue());
    refreshTokenMap.remove(authorization.getRefreshToken().getTokenValue());
    tokenMap.remove(id);
  }

  @Override
  public void remove(String scope, String username) {
    ApiAuthenticationToken authenticationToken = findByScopeAndUsername(scope, username);
    if (authenticationToken != null) {
      remove(authenticationToken);
    }
  }

  @Override
  public ApiAuthenticationToken findByScopeAndUsername(String scope, String username) {
    String id = scope + ":" + username;
    return tokenMap.get(id);
  }

  @Override
  public ApiAuthenticationToken findByAccessToken(String accessToken) {
    String id = accessTokenMap.get(accessToken);
    if (id != null) {
      return tokenMap.get(id);
    }
    return null;
  }

  @Override
  public ApiAuthenticationToken findByRefreshToken(String refreshToken) {
    String id = refreshTokenMap.get(refreshToken);
    if (id != null) {
      return tokenMap.get(id);
    }
    return null;
  }

}
