package top.bettercode.logging;

import org.springframework.boot.context.properties.ConfigurationProperties;
import top.bettercode.lang.util.RandomUtil;

/**
 * RequestLogging 配置
 *
 * @author Peter Wu
 */
@ConfigurationProperties("summer.logging.websocket")
public class WebsocketProperties {

  /**
   * 是否启用
   */
  private boolean enabled = true;
  /**
   * 认证token
   */
  private String token = RandomUtil.nextString(16);
  //--------------------------------------------


  public boolean isEnabled() {
    return enabled;
  }

  public void setEnabled(boolean enabled) {
    this.enabled = enabled;
  }

  public String getToken() {
    return token;
  }

  public void setToken(String token) {
    this.token = token;
  }
}