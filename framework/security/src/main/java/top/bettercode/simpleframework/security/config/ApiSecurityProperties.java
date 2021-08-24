package top.bettercode.simpleframework.security.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.util.AntPathMatcher;
import top.bettercode.lang.util.ArrayUtil;

/**
 * @author Peter Wu
 */
@ConfigurationProperties("summer.security")
public class ApiSecurityProperties {

  private Integer refreshTokenValiditySeconds = 60 * 60 * 24 * 30; // default 30 days.

  private Integer accessTokenValiditySeconds = 60 * 60 * 12; // default 12 hours.

  /**
   * security.url-filter.ignored.
   */
  private String[] urlFilterIgnored = new String[0];

  private SessionCreationPolicy sessionCreationPolicy = SessionCreationPolicy.STATELESS;

  /**
   * 是否禁用同源策略.
   */
  private Boolean frameOptionsDisable = true;

  private Boolean supportClientCache = true;


  private String app = "app";

  //--------------------------------------------
  public boolean ignored(String path) {
    if (ArrayUtil.isEmpty(urlFilterIgnored)) {
      return false;
    }
    AntPathMatcher antPathMatcher = new AntPathMatcher();
    for (String pattern : urlFilterIgnored) {
      if (antPathMatcher.match(pattern, path)) {
        return true;
      }
    }
    return false;
  }

  //--------------------------------------------

  public String getApp() {
    return app;
  }

  public void setApp(String app) {
    this.app = app;
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

  public String[] getUrlFilterIgnored() {
    return urlFilterIgnored;
  }

  public void setUrlFilterIgnored(String[] urlFilterIgnored) {
    this.urlFilterIgnored = urlFilterIgnored;
  }

  public SessionCreationPolicy getSessionCreationPolicy() {
    return sessionCreationPolicy;
  }

  public void setSessionCreationPolicy(
      SessionCreationPolicy sessionCreationPolicy) {
    this.sessionCreationPolicy = sessionCreationPolicy;
  }

  public Boolean getFrameOptionsDisable() {
    return frameOptionsDisable;
  }

  public void setFrameOptionsDisable(Boolean frameOptionsDisable) {
    this.frameOptionsDisable = frameOptionsDisable;
  }

  public Boolean getSupportClientCache() {
    return supportClientCache;
  }

  public void setSupportClientCache(Boolean supportClientCache) {
    this.supportClientCache = supportClientCache;
  }
}
