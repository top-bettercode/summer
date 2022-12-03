package top.bettercode.summer.tools.weixin.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * @author Peter Wu
 */
@ConfigurationProperties(prefix = "summer.wechat.corp")
public class CorpProperties extends WexinProperties implements ICorpProperties {

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

  //--------------------------------------------


  @Override
  public String getBasicAccessTokenUrl() {
    return "https://qyapi.weixin.qq.com/cgi-bin/gettoken?corpid={0}&corpsecret={1}";
  }

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
