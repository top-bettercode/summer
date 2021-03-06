package top.bettercode.summer.util.wechat.config;

/**
 * @author Peter Wu
 */
public class WexinProperties implements IWexinProperties {

  private String appId;
  private String secret;
  private String token;
  private String aesKey;

  /**
   * 请求连接超时时间毫秒数
   */
  private int connectTimeout = 10000;
  /**
   * 请求读取超时时间毫秒数
   */
  private int readTimeout = 10000;

  /**
   * access_token 及 jsapi_ticket 有效期7200秒
   */
  private Long cacheSeconds = 7200L;
  /**
   * 微信接口请求最大重试次数
   */
  private int maxRetries = 2;
  /**
   * 授权请求路径
   */
  private String oauthMappingPath;

  public String getAppId() {
    return appId;
  }

  public void setAppId(String appId) {
    this.appId = appId;
  }

  public String getSecret() {
    return secret;
  }

  public void setSecret(String secret) {
    this.secret = secret;
  }

  public String getToken() {
    return token;
  }

  public void setToken(String token) {
    this.token = token;
  }

  public String getAesKey() {
    return aesKey;
  }

  public void setAesKey(String aesKey) {
    this.aesKey = aesKey;
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

  public Long getCacheSeconds() {
    return cacheSeconds;
  }

  public void setCacheSeconds(Long cacheSeconds) {
    this.cacheSeconds = cacheSeconds;
  }

  @Override
  public int getMaxRetries() {
    return maxRetries;
  }

  public void setMaxRetries(int maxRetries) {
    this.maxRetries = maxRetries;
  }

  public String getOauthMappingPath() {
    return oauthMappingPath;
  }

  public WexinProperties setOauthMappingPath(String oauthMappingPath) {
    this.oauthMappingPath = oauthMappingPath;
    return this;
  }
}
