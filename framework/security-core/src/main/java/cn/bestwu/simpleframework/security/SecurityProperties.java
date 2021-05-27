package cn.bestwu.simpleframework.security;

import cn.bestwu.lang.util.ArrayUtil;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.util.AntPathMatcher;

/**
 * @author Peter Wu
 */
@ConfigurationProperties("summer.security")
public class SecurityProperties {

  /**
   * security.url-filter.ignored.
   */
  private String[] urlFilterIgnored = new String[0];

  private SessionCreationPolicy sessionCreationPolicy = SessionCreationPolicy.STATELESS;

  /**
   * 是否禁用同源策略.
   */
  private Boolean frameOptionsDisable = true;

  private Boolean supportClientCache=true;

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
