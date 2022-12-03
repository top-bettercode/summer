package top.bettercode.summer.config;

import java.nio.charset.StandardCharsets;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.util.DigestUtils;

/**
 * 日志访问权限 配置
 *
 * @author Peter Wu
 */
@ConfigurationProperties("summer.management.auth")
public class ManagementAuthProperties {

  private String[] pattern = {};
  /**
   * 访问授权有效时间，单位：秒
   */
  private int maxAge = -1;
  private String username = "madmin";
  private String password;

  //--------------------------------------------
  public String getAuthKey() {
    return DigestUtils.md5DigestAsHex(
        "${managementAuthProperties.username}:${managementAuthProperties.password}".getBytes(
            StandardCharsets.UTF_8));
  }
  //--------------------------------------------

  public String[] getPattern() {
    return pattern;
  }

  public void setPattern(String[] pattern) {
    this.pattern = pattern;
  }

  public int getMaxAge() {
    return maxAge;
  }

  public void setMaxAge(int maxAge) {
    this.maxAge = maxAge;
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