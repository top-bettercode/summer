package top.bettercode.simpleframework.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.util.StringUtils;
import org.springframework.web.context.request.NativeWebRequest;

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
   * 接口版本号header参数名称.
   */
  private String versionName = "api-version";

  /**
   * 接口版本号.
   */
  private String version = "v1.0";

  /**
   * 接口版本号header参数名称.
   */
  private String versionNoName = "api-version-no";
  /**
   * 接口版本号.
   */
  private String versionNo = "1";

  /**
   * 字段效验异常信息分隔符.
   */
  private String constraintViolationSeparator = "";

  /**
   * Response wrap header参数名称
   */
  private String wrapName = "wrap-response";
  /**
   * Response ok header参数名称
   */
  private String okName = "ok-response";

  /**
   * 表单防重复提交，key有效时间
   */
  private Long formExpireSeconds = 10L;

  //--------------------------------------------
  public Boolean wrapEnable(NativeWebRequest request) {
    String wrapResponse = request.getHeader(wrapName);
    if (StringUtils.hasText(wrapResponse)) {
      return "true".equals(wrapResponse);
    } else {
      return wrapEnable;
    }
  }

  public Boolean okEnable(NativeWebRequest request) {
    String okResponse = request.getHeader(okName);
    if (StringUtils.hasText(okResponse)) {
      return "true".equals(okResponse);
    } else {
      return okEnable;
    }
  }

  //--------------------------------------------


  public Long getFormExpireSeconds() {
    return formExpireSeconds;
  }

  public void setFormExpireSeconds(Long formExpireSeconds) {
    this.formExpireSeconds = formExpireSeconds;
  }

  public String getProjectName() {
    return projectName;
  }

  public void setProjectName(String projectName) {
    this.projectName = projectName;
  }

  public String getWrapName() {
    return wrapName;
  }

  public void setWrapName(String wrapName) {
    this.wrapName = wrapName;
  }

  public String getOkName() {
    return okName;
  }

  public void setOkName(String okName) {
    this.okName = okName;
  }

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
