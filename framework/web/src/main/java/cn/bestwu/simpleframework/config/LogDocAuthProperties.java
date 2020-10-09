package cn.bestwu.simpleframework.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.util.AntPathMatcher;

/**
 * 日志访问权限 配置
 *
 * @author Peter Wu
 */
@ConfigurationProperties("logging.auth")
public class LogDocAuthProperties {

  private String[] pattern = {"/logs/**", "/doc/**"};
  private String username = "logAdmin";
  private String password;
  private final AntPathMatcher antPathMatcher = new AntPathMatcher();

  public boolean match(String uri) {
    for (String pattern : pattern) {
      if (antPathMatcher.match(pattern, uri)) {
        return true;
      }
    }
    return false;
  }

  public String[] getPattern() {
    return pattern;
  }

  public void setPattern(String[] pattern) {
    this.pattern = pattern;
  }

  public String getUsername() {
    return username;
  }

  public void setUsername(String username) {
    this.username = username;
  }

  public String getPassword() {
    return password;
  }

  public void setPassword(String password) {
    this.password = password;
  }
}