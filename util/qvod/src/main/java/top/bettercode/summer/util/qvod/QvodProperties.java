package top.bettercode.summer.util.qvod;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * @author Peter Wu
 */
@ConfigurationProperties(prefix = "summer.qvod")
public class QvodProperties {


  private String secretId;

  private String secretKey;
  /**
   * HTTP 请求头：X-TC-Region。地域参数，用来标识希望操作哪个地域的数据。接口接受的地域取值参考接口文档中输入参数公共参数 Region
   * 的说明。注意：某些接口不需要传递该参数，接口文档中会对此特别说明，此时即使传递该参数也不会生效。
   */
  private String region;
  /**
   * 视频后续任务处理操作任务流模板
   */
  private String procedure;

  /**
   * 签名有效时长，单位：秒 有效时长最大取值为7776000，即90天。
   */
  private long validSeconds = 60 * 60L;

  /**
   * 请求连接超时时间毫秒数
   */
  private int connectTimeout = 10000;
  /**
   * 请求读取超时时间毫秒数
   */
  private int readTimeout = 10000;

  public String getSecretId() {
    return secretId;
  }

  public void setSecretId(String secretId) {
    this.secretId = secretId;
  }

  public String getSecretKey() {
    return secretKey;
  }

  public void setSecretKey(String secretKey) {
    this.secretKey = secretKey;
  }

  public String getRegion() {
    return region;
  }

  public void setRegion(String region) {
    this.region = region;
  }

  public String getProcedure() {
    return procedure;
  }

  public void setProcedure(String procedure) {
    this.procedure = procedure;
  }

  public long getValidSeconds() {
    return validSeconds;
  }

  public void setValidSeconds(long validSeconds) {
    this.validSeconds = validSeconds;
  }

  public int getConnectTimeout() {
    return connectTimeout;
  }

  public QvodProperties setConnectTimeout(int connectTimeout) {
    this.connectTimeout = connectTimeout;
    return this;
  }

  public int getReadTimeout() {
    return readTimeout;
  }

  public QvodProperties setReadTimeout(int readTimeout) {
    this.readTimeout = readTimeout;
    return this;
  }
}
