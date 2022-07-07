package top.bettercode.summer.util.wechat.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * @author Peter Wu
 */
@ConfigurationProperties(prefix = "summer.wechat")
public class OffiaccountProperties extends WexinProperties implements IOffiaccountProperties {

  private Boolean userUnionid= false;

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
  public Boolean getUserUnionid() {
    return userUnionid;
  }

  public OffiaccountProperties setUserUnionid(Boolean userUnionid) {
    this.userUnionid = userUnionid;
    return this;
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
