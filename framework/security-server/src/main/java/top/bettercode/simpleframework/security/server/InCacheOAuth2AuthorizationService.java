package top.bettercode.simpleframework.security.server;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.lang.Nullable;
import org.springframework.security.oauth2.core.OAuth2AccessToken;
import org.springframework.security.oauth2.core.OAuth2AuthorizationCode;
import org.springframework.security.oauth2.core.OAuth2RefreshToken;
import org.springframework.security.oauth2.core.OAuth2TokenType;
import org.springframework.security.oauth2.core.endpoint.OAuth2ParameterNames;
import org.springframework.security.oauth2.server.authorization.OAuth2Authorization;
import org.springframework.security.oauth2.server.authorization.OAuth2AuthorizationService;
import org.springframework.util.Assert;

public final class InCacheOAuth2AuthorizationService implements OAuth2AuthorizationService {

  private final Cache cacheAuthorizations;
  private static final String oauth2AuthorizationKey = "oauth_2_authorization:";

  public InCacheOAuth2AuthorizationService(CacheManager cacheManager) {
    this(cacheManager, "");
  }

  public InCacheOAuth2AuthorizationService(CacheManager cacheManager, String prefix) {
    this(Collections.emptyList(), prefix, cacheManager);
  }

  public InCacheOAuth2AuthorizationService(CacheManager cacheManager, String prefix,
      OAuth2Authorization... authorizations) {
    this(Arrays.asList(authorizations), prefix, cacheManager);
  }


  public InCacheOAuth2AuthorizationService(List<OAuth2Authorization> authorizations, String prefix,
      CacheManager cacheManager) {
    if (prefix == null) {
      prefix = "";
    }
    this.cacheAuthorizations = cacheManager.getCache(prefix + oauth2AuthorizationKey);
    Assert.notNull(authorizations, "authorizations cannot be null");
    authorizations.forEach(authorization -> {
      Assert.notNull(authorization, "authorization cannot be null");
      Assert.isTrue(this.cacheAuthorizations.get(authorization.getId()) == null,
          "The authorization must be unique. Found duplicate identifier: " + authorization.getId());
      save(authorization);
    });
  }

  @Override
  public void save(OAuth2Authorization authorization) {
    Assert.notNull(authorization, "authorization cannot be null");
    String id = authorization.getId();
    this.cacheAuthorizations.put(id, authorization);
    this.cacheAuthorizations.put(
        OAuth2ParameterNames.STATE + ":" + authorization.getAttribute(OAuth2ParameterNames.STATE),
        id);
    OAuth2Authorization.Token<OAuth2AuthorizationCode> authorizationCode = authorization.getToken(
        OAuth2AuthorizationCode.class);
    if (authorizationCode != null) {
      this.cacheAuthorizations.put(
          OAuth2ParameterNames.CODE + ":" + authorizationCode.getToken().getTokenValue(), id);
    }
    OAuth2Authorization.Token<OAuth2AccessToken> accessToken = authorization.getToken(
        OAuth2AccessToken.class);
    if (accessToken != null) {
      this.cacheAuthorizations.put(
          OAuth2TokenType.ACCESS_TOKEN.getValue() + ":" + accessToken.getToken().getTokenValue(),
          id);
    }
    OAuth2Authorization.Token<OAuth2RefreshToken> refreshToken = authorization.getToken(
        OAuth2RefreshToken.class);
    if (refreshToken != null) {
      this.cacheAuthorizations.put(
          OAuth2TokenType.REFRESH_TOKEN.getValue() + ":" + refreshToken.getToken().getTokenValue(),
          id);
    }
  }

  @Override
  public void remove(OAuth2Authorization authorization) {
    Assert.notNull(authorization, "authorization cannot be null");
    this.cacheAuthorizations.evict(authorization.getId());
  }

  @Nullable
  @Override
  public OAuth2Authorization findById(String id) {
    Assert.hasText(id, "id cannot be empty");
    return this.cacheAuthorizations.get(id,
        org.springframework.security.oauth2.server.authorization.OAuth2Authorization.class);
  }

  @Nullable
  @Override
  public OAuth2Authorization findByToken(String token, @Nullable OAuth2TokenType tokenType) {
    Assert.hasText(token, "token cannot be empty");
    if (tokenType == null) {
      org.springframework.security.oauth2.server.authorization.OAuth2Authorization oauth2Authorization = matchesState(
          token);
      if (oauth2Authorization == null) {
        oauth2Authorization = matchesAuthorizationCode(token);
      }
      if (oauth2Authorization == null) {
        oauth2Authorization = matchesAccessToken(token);
      }
      if (oauth2Authorization == null) {
        oauth2Authorization = matchesRefreshToken(token);
      }
      return oauth2Authorization;
    } else if (OAuth2ParameterNames.STATE.equals(tokenType.getValue())) {
      return matchesState(token);
    } else if (OAuth2ParameterNames.CODE.equals(tokenType.getValue())) {
      return matchesAuthorizationCode(token);
    } else if (OAuth2TokenType.ACCESS_TOKEN.equals(tokenType)) {
      return matchesAccessToken(token);
    } else if (OAuth2TokenType.REFRESH_TOKEN.equals(tokenType)) {
      return matchesRefreshToken(token);
    }
    return null;
  }

  private OAuth2Authorization matchesState(String token) {
    String id = this.cacheAuthorizations.get(OAuth2ParameterNames.STATE + ":" + token,
        String.class);
    return id == null ? null : findById(id);
  }

  private OAuth2Authorization matchesAuthorizationCode(String token) {
    String id = this.cacheAuthorizations.get(OAuth2ParameterNames.CODE + ":" + token,
        String.class);
    return id == null ? null : findById(id);
  }

  private OAuth2Authorization matchesAccessToken(String token) {
    String id = this.cacheAuthorizations.get(OAuth2TokenType.ACCESS_TOKEN.getValue() + ":" + token,
        String.class);
    return id == null ? null : findById(id);
  }

  private OAuth2Authorization matchesRefreshToken(String token) {
    String id = this.cacheAuthorizations.get(OAuth2TokenType.REFRESH_TOKEN.getValue() + ":" + token,
        String.class);
    return id == null ? null : findById(id);
  }
}
