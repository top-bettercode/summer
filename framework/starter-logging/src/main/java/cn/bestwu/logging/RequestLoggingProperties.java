package cn.bestwu.logging;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.util.AntPathMatcher;

/**
 * RequestLogging 配置
 *
 * @author Peter Wu
 */
@ConfigurationProperties("summer.logging.request")
public class RequestLoggingProperties {

  /**
   * 是否启用
   */
  private boolean enabled = true;
  /**
   * 是否包含请求体
   */
  private boolean includeRequestBody = true;
  /**
   * 是否包含响应体
   */
  private boolean includeResponseBody = true;
  /**
   * 是否包含错误追踪栈
   */
  private boolean includeTrace = true;
  /**
   * 是否格式化日志
   */
  private boolean format = true;
  /**
   * 强制记录日志
   */
  private boolean forceRecord = false;
  /**
   * 请求超时警报时间秒数
   */
  private Integer timeoutAlarmSeconds = 2;
  /**
   * 忽略超时接口
   */
  private String[] ignoredTimeoutPath = {};
  /**
   * 需要记录日志的 Controller类名前缀.如果为空记录所有 Controller类.
   */
  private String[] handlerTypePrefix = new String[0];
  /**
   * Comma-separated list of paths to exclude from the default logging paths.
   */
  private String[] ignored = {"*.js", "*.gif", "*.jpg", "*.png", "*.css", "*.ico"};
  /**
   * 额外包含的需要记录日志的路径
   */
  private String[] includePath = {};
  /**
   * 加密参数名
   */
  private String[] encryptParameters = new String[0];
  /**
   * 加密请求头名
   */
  private String[] encryptHeaders = new String[0];
  /**
   * 过滤不记录为ERROR日志的状态码
   */
  private Integer[] ignoredErrorStatusCode = {400, 401, 403, 404, 405, 406, 409,
      422};
  //--------------------------------------------

  private final AntPathMatcher antPathMatcher = new AntPathMatcher();

  public boolean matchIgnored(String uri) {
    for (String pattern : ignored) {
      if (antPathMatcher.match(pattern, uri)) {
        return true;
      }
    }
    return false;
  }

  public boolean isEnabled() {
    return enabled;
  }

  public void setEnabled(boolean enabled) {
    this.enabled = enabled;
  }

  public boolean isIncludeRequestBody() {
    return includeRequestBody;
  }

  public void setIncludeRequestBody(boolean includeRequestBody) {
    this.includeRequestBody = includeRequestBody;
  }

  public boolean isIncludeResponseBody() {
    return includeResponseBody;
  }

  public void setIncludeResponseBody(boolean includeResponseBody) {
    this.includeResponseBody = includeResponseBody;
  }

  public boolean isIncludeTrace() {
    return includeTrace;
  }

  public void setIncludeTrace(boolean includeTrace) {
    this.includeTrace = includeTrace;
  }

  public boolean isFormat() {
    return format;
  }

  public void setFormat(boolean format) {
    this.format = format;
  }

  public Integer getTimeoutAlarmSeconds() {
    return timeoutAlarmSeconds;
  }

  public void setTimeoutAlarmSeconds(Integer timeoutAlarmSeconds) {
    this.timeoutAlarmSeconds = timeoutAlarmSeconds;
  }

  public String[] getIgnoredTimeoutPath() {
    return ignoredTimeoutPath;
  }

  public void setIgnoredTimeoutPath(String[] ignoredTimeoutPath) {
    this.ignoredTimeoutPath = ignoredTimeoutPath;
  }

  public boolean isForceRecord() {
    return forceRecord;
  }

  public void setForceRecord(boolean forceRecord) {
    this.forceRecord = forceRecord;
  }

  public String[] getHandlerTypePrefix() {
    return handlerTypePrefix;
  }

  public void setHandlerTypePrefix(String[] handlerTypePrefix) {
    this.handlerTypePrefix = handlerTypePrefix;
  }

  public String[] getIgnored() {
    return ignored;
  }

  public void setIgnored(String[] ignored) {
    this.ignored = ignored;
  }

  public String[] getIncludePath() {
    return includePath;
  }

  public void setIncludePath(String[] includePath) {
    this.includePath = includePath;
  }

  public String[] getEncryptParameters() {
    return encryptParameters;
  }

  public void setEncryptParameters(String[] encryptParameters) {
    this.encryptParameters = encryptParameters;
  }

  public String[] getEncryptHeaders() {
    return encryptHeaders;
  }

  public void setEncryptHeaders(String[] encryptHeaders) {
    this.encryptHeaders = encryptHeaders;
  }

  public Integer[] getIgnoredErrorStatusCode() {
    return ignoredErrorStatusCode;
  }

  public void setIgnoredErrorStatusCode(Integer[] ignoredErrorStatusCode) {
    this.ignoredErrorStatusCode = ignoredErrorStatusCode;
  }
}