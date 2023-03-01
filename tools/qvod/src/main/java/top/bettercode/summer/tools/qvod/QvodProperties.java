package top.bettercode.summer.tools.qvod;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * @author Peter Wu
 */
@ConfigurationProperties(prefix = "summer.qvod")
public class QvodProperties {

  /**
   * 点播帐号APP ID
   */
  private Long appId;

  /**
   * 防盗链 Key
   */
  private String securityChainKey;

  /**
   * 视频访问有效时长，单位秒,默认一天有效时间
   */
  private long accessValidSeconds = 60 * 60 * 24L;
  /**
   * 最多允许多少个不同 IP 的终端播放，以十进制表示，最大值为9，不填表示不做限制 当限制 URL 只能被1个人播放时，建议 rlimit
   * 不要严格限制成1（例如可设置为3），因为移动端断网后重连 IP 可能改变
   */
  private Integer rlimit = 9;
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
   * 转码模板
   */
  private Long[] templateIds;
  /**
   * 图片即时处理模板ID
   */
  private String picTemplateId;

  /**
   * 上传签名有效时长，单位：秒 有效时长最大取值为7776000，即90天。默认2小时.
   */
  private long uploadValidSeconds = 2 * 60 * 60L;

  /**
   * 请求连接超时时间毫秒数
   */
  private int connectTimeout = 10000;
  /**
   * 请求读取超时时间毫秒数
   */
  private int readTimeout = 10000;
  /**
   * 文件分类，默认为0
   */
  private int classId = 0;

  public Long getAppId() {
    return appId;
  }

  public void setAppId(Long appId) {
    this.appId = appId;
  }

  public String getSecurityChainKey() {
    return securityChainKey;
  }

  public void setSecurityChainKey(String securityChainKey) {
    this.securityChainKey = securityChainKey;
  }

  public long getAccessValidSeconds() {
    return accessValidSeconds;
  }

  public void setAccessValidSeconds(long accessValidSeconds) {
    this.accessValidSeconds = accessValidSeconds;
  }

  public Integer getRlimit() {
    return rlimit;
  }

  public void setRlimit(Integer rlimit) {
    this.rlimit = rlimit;
  }

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

  public Long[] getTemplateIds() {
    return templateIds;
  }

  public void setTemplateIds(Long[] templateIds) {
    this.templateIds = templateIds;
  }

  public String getPicTemplateId() {
    return picTemplateId;
  }

  public void setPicTemplateId(String picTemplateId) {
    this.picTemplateId = picTemplateId;
  }

  public long getUploadValidSeconds() {
    return uploadValidSeconds;
  }

  public void setUploadValidSeconds(long uploadValidSeconds) {
    this.uploadValidSeconds = uploadValidSeconds;
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

  public int getClassId() {
    return classId;
  }

  public void setClassId(int classId) {
    this.classId = classId;
  }
}
