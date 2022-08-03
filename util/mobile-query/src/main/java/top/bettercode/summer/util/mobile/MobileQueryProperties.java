package top.bettercode.summer.util.mobile;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * @author Peter Wu
 */
@ConfigurationProperties(prefix = "summer.mobile-query")
public class MobileQueryProperties {

  /**
   * 接口地址
   */
  private String url = "https://service-mxd4ekp3-1255701024.sh.apigw.tencentcs.com/release/superapi/super/flashsdk/mobile-query";
  /**
   * 应用标识
   */
  private String appId;
  /**
   * 应用密钥
   */
  private String appKey;

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

  public MobileQueryProperties setUrl(String url) {
    this.url = url;
    return this;
  }

  public String getAppId() {
    return appId;
  }

  public MobileQueryProperties setAppId(String appId) {
    this.appId = appId;
    return this;
  }

  public String getAppKey() {
    return appKey;
  }

  public MobileQueryProperties setAppKey(String appKey) {
    this.appKey = appKey;
    return this;
  }

  public int getConnectTimeout() {
    return connectTimeout;
  }

  public MobileQueryProperties setConnectTimeout(int connectTimeout) {
    this.connectTimeout = connectTimeout;
    return this;
  }

  public int getReadTimeout() {
    return readTimeout;
  }

  public MobileQueryProperties setReadTimeout(int readTimeout) {
    this.readTimeout = readTimeout;
    return this;
  }
}
