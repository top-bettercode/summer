package cn.bestwu.simpleframework.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * @author Peter Wu
 */
@ConfigurationProperties("summer.web")
public class WebProperties {

  /**
   * 项目名称.
   */
  private String projectName;
  /**
   * 响应结果是否包一层{\"data\":52,\"message\":\"\",\"status\":\"200\"}样式的格式.
   */
  private Boolean wrapEnable = true;

  /**
   * http响应状态码统一为200.
   */
  private Boolean okEnable = true;
  /**
   * 接口版本号名称.
   */
  private String versionName = "apiVersion";

  /**
   * 接口版本号.
   */
  private String version = "v1.0";

  /**
   * 接口版本号名称.
   */
  private String versionNoName = "apiVersionNo";
  /**
   * 接口版本号.
   */
  private String versionNo = "1";

  /**
   * 字段效验异常信息分隔符.
   */
  private String constraintViolationSeparator = "";

  public Boolean getWrapEnable() {
    return wrapEnable;
  }

  public void setWrapEnable(Boolean wrapEnable) {
    this.wrapEnable = wrapEnable;
  }

  public Boolean getOkEnable() {
    return okEnable;
  }

  public void setOkEnable(Boolean okEnable) {
    this.okEnable = okEnable;
  }

  public String getVersionName() {
    return versionName;
  }

  public void setVersionName(String versionName) {
    this.versionName = versionName;
  }

  public String getVersion() {
    return version;
  }

  public void setVersion(String version) {
    this.version = version;
  }

  public String getVersionNoName() {
    return versionNoName;
  }

  public void setVersionNoName(String versionNoName) {
    this.versionNoName = versionNoName;
  }

  public String getVersionNo() {
    return versionNo;
  }

  public void setVersionNo(String versionNo) {
    this.versionNo = versionNo;
  }

  public String getConstraintViolationSeparator() {
    return constraintViolationSeparator;
  }

  public void setConstraintViolationSeparator(String constraintViolationSeparator) {
    this.constraintViolationSeparator = constraintViolationSeparator;
  }
}