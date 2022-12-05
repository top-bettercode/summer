package top.bettercode.summer.tools.rapidauth;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * @author Peter Wu
 */
@ConfigurationProperties(prefix = "summer.rapidauth")
public class RapidauthProperties {

  /**
   * 接口地址
   */
  private String url = "https://yun.tim.qq.com/v5/rapidauth/validate?sdkappid={0}&random={1}";
  /**
   * 腾讯应用ID
   */
  private String sdkappid;
  /**
   * 应用密钥
   */
  private String appkey;

  /**
   * 请求连接超时时间毫秒数
   */
  private int connectTimeout = 10000;
  /**
   * 请求读取超时时间毫秒数
   */
  private int readTimeout = 10000;

  public String getUrl() {
    return url;
  }

  public RapidauthProperties setUrl(String url) {
    this.url = url;
    return this;
  }

  public String getSdkappid() {
    return sdkappid;
  }

  public void setSdkappid(String sdkappid) {
    this.sdkappid = sdkappid;
  }

  public String getAppkey() {
    return appkey;
  }

  public void setAppkey(String appkey) {
    this.appkey = appkey;
  }

  public int getConnectTimeout() {
    return connectTimeout;
  }

  public RapidauthProperties setConnectTimeout(int connectTimeout) {
    this.connectTimeout = connectTimeout;
    return this;
  }

  public int getReadTimeout() {
    return readTimeout;
  }

  public RapidauthProperties setReadTimeout(int readTimeout) {
    this.readTimeout = readTimeout;
    return this;
  }


}
