package cn.bestwu.simpleframework.security;

import java.util.Collections;
import java.util.Set;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * @author Peter Wu
 */
@ConfigurationProperties(prefix = "security.oauth2.client")
public class ClientDetailsProperties {

  private String clientId;

  private String clientSecret;

  private Set<String> scope = Collections.emptySet();

  private Set<String> authorizedGrantTypes = Collections.emptySet();

  private Set<String> autoApproveScopes;

  private Integer refreshTokenValiditySeconds = 60 * 60 * 24 * 30; // default 30 days.

  private Integer accessTokenValiditySeconds = 60 * 60 * 12; // default 12 hours.

  public String getClientId() {
    return clientId;
  }

  public void setClientId(String clientId) {
    this.clientId = clientId;
  }

  public String getClientSecret() {
    return clientSecret;
  }

  public void setClientSecret(String clientSecret) {
    this.clientSecret = clientSecret;
  }

  public Set<String> getScope() {
    return scope;
  }

  public void setScope(Set<String> scope) {
    this.scope = scope;
  }

  public Set<String> getAuthorizedGrantTypes() {
    return authorizedGrantTypes;
  }

  public void setAuthorizedGrantTypes(Set<String> authorizedGrantTypes) {
    this.authorizedGrantTypes = authorizedGrantTypes;
  }

  public Set<String> getAutoApproveScopes() {
    return autoApproveScopes;
  }

  public void setAutoApproveScopes(Set<String> autoApproveScopes) {
    this.autoApproveScopes = autoApproveScopes;
  }

  public Integer getRefreshTokenValiditySeconds() {
    return refreshTokenValiditySeconds;
  }

  public void setRefreshTokenValiditySeconds(Integer refreshTokenValiditySeconds) {
    this.refreshTokenValiditySeconds = refreshTokenValiditySeconds;
  }

  public Integer getAccessTokenValiditySeconds() {
    return accessTokenValiditySeconds;
  }

  public void setAccessTokenValiditySeconds(Integer accessTokenValiditySeconds) {
    this.accessTokenValiditySeconds = accessTokenValiditySeconds;
  }
}
