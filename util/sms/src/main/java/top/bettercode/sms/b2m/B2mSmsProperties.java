package top.bettercode.sms.b2m;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * 亿美软通短信平台 配置信息
 *
 * @author Peter Wu
 */
@ConfigurationProperties(prefix = "summer.sms.b2m")
public class B2mSmsProperties {

  private String url="http://www.btom.cn:8080";
  private String appId;
  private String secretKey;
  /**
   * 请求连接超时时间毫秒数
   */
  private int connectTimeout = 10000;
  /**
   * 请求读取超时时间毫秒数
   */
  private int readTimeout = 10000;
  /**
   * 请求有效时间(必填)[单位:秒]
   */
  private int requestValidPeriod=60;

  public String getUrl() {
    return url;
  }

  public void setUrl(String url) {
    this.url = url;
  }

  public String getAppId() {
    return appId;
  }

  public void setAppId(String appId) {
    this.appId = appId;
  }

  public String getSecretKey() {
    return secretKey;
  }

  public void setSecretKey(String secretKey) {
    this.secretKey = secretKey;
  }

  public int getConnectTimeout() {
    return connectTimeout;
  }

  public void setConnectTimeout(int connectTimeout) {
    this.connectTimeout = connectTimeout;
  }

  public int getReadTimeout() {
    return readTimeout;
  }

  public void setReadTimeout(int readTimeout) {
    this.readTimeout = readTimeout;
  }

  public int getRequestValidPeriod() {
    return requestValidPeriod;
  }

  public void setRequestValidPeriod(int requestValidPeriod) {
    this.requestValidPeriod = requestValidPeriod;
  }
}
