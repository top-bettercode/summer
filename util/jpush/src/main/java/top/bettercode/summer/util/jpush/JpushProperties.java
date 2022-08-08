package top.bettercode.summer.util.jpush;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * @author Peter Wu
 */
@ConfigurationProperties(prefix = "summer.jpush")
public class JpushProperties {

  /**
   * 接口地址
   */
  private String url = "https://api.jpush.cn/v3";
  /**
   * appKey
   */
  private String appKey;
  /**
   * masterSecret
   */
  private String masterSecret;
  /**
   * APNs 是否生产环境 该字段仅对 iOS 的 Notification 有效，如果不指定则为推送生产环境。注意：JPush 服务端 SDK 默认设置为推送 “开发环境”。
   * true：表示推送生产环境。 false：表示推送开发环境。
   */
  private boolean apnsProduction;
  /**
   * 离线消息保留时长 (秒) 推送当前用户不在线时，为该用户保留多长时间的离线消息，以便其上线时再次推送。 默认 86400 （1 天），普通用户最长 3 天， VIP 用户最长 10
   * 天。设置为 0 表示不保留离线消息，只有推送当前在线的用户可以收到。 该字段对 iOS 的 Notification 消息无效。
   */
  private long timeToLive = 60 * 60 * 24 * 3L;

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

  public void setUrl(String url) {
    this.url = url;
  }

  public String getAppKey() {
    return appKey;
  }

  public void setAppKey(String appKey) {
    this.appKey = appKey;
  }

  public String getMasterSecret() {
    return masterSecret;
  }

  public void setMasterSecret(String masterSecret) {
    this.masterSecret = masterSecret;
  }

  public boolean isApnsProduction() {
    return apnsProduction;
  }

  public void setApnsProduction(boolean apnsProduction) {
    this.apnsProduction = apnsProduction;
  }

  public long getTimeToLive() {
    return timeToLive;
  }

  public void setTimeToLive(long timeToLive) {
    this.timeToLive = timeToLive;
  }

  public int getConnectTimeout() {
    return connectTimeout;
  }

  public JpushProperties setConnectTimeout(int connectTimeout) {
    this.connectTimeout = connectTimeout;
    return this;
  }

  public int getReadTimeout() {
    return readTimeout;
  }

  public JpushProperties setReadTimeout(int readTimeout) {
    this.readTimeout = readTimeout;
    return this;
  }
}
