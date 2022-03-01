package top.bettercode.summer.util.wechat.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * @author Peter Wu
 */
@ConfigurationProperties(prefix = "summer.wechat")
public class OffiaccountProperties extends WexinProperties {

  public static final String OAUTH_URL = "/wechat/oauth";

  /**
   * 项目部署的URL地址
   */
  private String appBaseUrl;
  /**
   * 微信前端基础地址
   */
  private String wechatBaseUrl;
  /**
   * 微信前端授权页面地址
   */
  private String wechatWebOauthUrl = "";

  public String getOauthUrl() {
    return appBaseUrl + OAUTH_URL;
  }

  public String redirectUrl(String token, String openId, boolean hasBound) {
    return "redirect:" + wechatUrl(token, openId, hasBound);
  }

  public String wechatUrl(String token, String openId, boolean hasBound) {
    return wechatBaseUrl + wechatWebOauthUrl + "?access_token=" + (token == null ? ""
        : token) + "&" + OPEN_ID_NAME + "=" + openId + "&hasBound=" + hasBound + "&_timer="
        + System.currentTimeMillis();
  }
  //--------------------------------------------

  public String getAppBaseUrl() {
    return appBaseUrl;
  }

  public void setAppBaseUrl(String appBaseUrl) {
    this.appBaseUrl = appBaseUrl;
  }

  public String getWechatBaseUrl() {
    return wechatBaseUrl;
  }

  public void setWechatBaseUrl(String wechatBaseUrl) {
    this.wechatBaseUrl = wechatBaseUrl;
  }

  public String getWechatWebOauthUrl() {
    return wechatWebOauthUrl;
  }

  public void setWechatWebOauthUrl(String wechatWebOauthUrl) {
    this.wechatWebOauthUrl = wechatWebOauthUrl;
  }
}
