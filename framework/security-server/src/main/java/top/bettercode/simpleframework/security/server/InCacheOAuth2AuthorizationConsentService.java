package top.bettercode.simpleframework.security.server;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.lang.Nullable;
import org.springframework.security.oauth2.server.authorization.OAuth2AuthorizationConsent;
import org.springframework.security.oauth2.server.authorization.OAuth2AuthorizationConsentService;
import org.springframework.util.Assert;

public final class InCacheOAuth2AuthorizationConsentService implements
    OAuth2AuthorizationConsentService {

  private final Cache cacheAuthorizationConsents;
  private static final String oauth2AuthorizationConsentKey = "oauth_2_authorization_consent:";


  public InCacheOAuth2AuthorizationConsentService(CacheManager cacheManager) {
    this(cacheManager, "");
  }

  public InCacheOAuth2AuthorizationConsentService(CacheManager cacheManager, String prefix) {
    this(cacheManager, prefix, Collections.emptyList());
  }


  public InCacheOAuth2AuthorizationConsentService(CacheManager cacheManager, String prefix,
      OAuth2AuthorizationConsent... authorizationConsents) {
    this(cacheManager, prefix, Arrays.asList(authorizationConsents));
  }

  /**
   * Constructs an {@code InMemoryOAuth2AuthorizationConsentService} using the provided parameters.
   *
   * @param authorizationConsents the authorization consent(s)
   */
  public InCacheOAuth2AuthorizationConsentService(CacheManager cacheManager, String prefix,
      List<OAuth2AuthorizationConsent> authorizationConsents) {
    if (prefix == null) {
      prefix = "";
    }
    this.cacheAuthorizationConsents = cacheManager.getCache(prefix + oauth2AuthorizationConsentKey);
    Assert.notNull(authorizationConsents, "authorizationConsents cannot be null");
    authorizationConsents.forEach(authorizationConsent -> {
      Assert.notNull(authorizationConsent, "authorizationConsent cannot be null");
      int id = getId(authorizationConsent);
      Assert.isTrue(this.cacheAuthorizationConsents.get(id) == null,
          "The authorizationConsent must be unique. Found duplicate, with registered client id: ["
              + authorizationConsent.getRegisteredClientId()
              + "] and principal name: [" + authorizationConsent.getPrincipalName() + "]");
      save(authorizationConsent);
    });
  }

  @Override
  public void save(OAuth2AuthorizationConsent authorizationConsent) {
    Assert.notNull(authorizationConsent, "authorizationConsent cannot be null");
    int id = getId(authorizationConsent);
    this.cacheAuthorizationConsents.put(id, authorizationConsent);
  }

  @Override
  public void remove(OAuth2AuthorizationConsent authorizationConsent) {
    Assert.notNull(authorizationConsent, "authorizationConsent cannot be null");
    int id = getId(authorizationConsent);
    this.cacheAuthorizationConsents.evict(id);
  }

  @Override
  @Nullable
  public OAuth2AuthorizationConsent findById(String registeredClientId, String principalName) {
    Assert.hasText(registeredClientId, "registeredClientId cannot be empty");
    Assert.hasText(principalName, "principalName cannot be empty");
    int id = getId(registeredClientId, principalName);
    return this.cacheAuthorizationConsents.get(id, OAuth2AuthorizationConsent.class);
  }

  private static int getId(String registeredClientId, String principalName) {
    return Objects.hash(registeredClientId, principalName);
  }

  private static int getId(OAuth2AuthorizationConsent authorizationConsent) {
    return getId(authorizationConsent.getRegisteredClientId(),
        authorizationConsent.getPrincipalName());
  }

}
