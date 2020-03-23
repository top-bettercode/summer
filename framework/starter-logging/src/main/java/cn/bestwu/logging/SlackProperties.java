package cn.bestwu.logging;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * slack 配置
 *
 * @author Peter Wu
 */
@ConfigurationProperties("logging.slack")
public class SlackProperties {

  private boolean sendFile = true;
  private String[] logger = {"root"};
  private String[] ignoredWarnLogger = {};
  private String authToken;
  private String channel;
  private Integer sendDelaySeconds = 5;
  private Integer cyclicBufferSize = 1;

  public boolean isSendFile() {
    return sendFile;
  }

  public void setSendFile(boolean sendFile) {
    this.sendFile = sendFile;
  }

  public String[] getLogger() {
    return logger;
  }

  public void setLogger(String[] logger) {
    this.logger = logger;
  }

  public String[] getIgnoredWarnLogger() {
    return ignoredWarnLogger;
  }

  public void setIgnoredWarnLogger(String[] ignoredWarnLogger) {
    this.ignoredWarnLogger = ignoredWarnLogger;
  }

  public String getAuthToken() {
    return authToken;
  }

  public void setAuthToken(String authToken) {
    this.authToken = authToken;
  }

  public String getChannel() {
    return channel;
  }

  public void setChannel(String channel) {
    this.channel = channel;
  }

  public Integer getSendDelaySeconds() {
    return sendDelaySeconds;
  }

  public void setSendDelaySeconds(Integer sendDelaySeconds) {
    this.sendDelaySeconds = sendDelaySeconds;
  }

  public Integer getCyclicBufferSize() {
    return cyclicBufferSize;
  }

  public void setCyclicBufferSize(Integer cyclicBufferSize) {
    this.cyclicBufferSize = cyclicBufferSize;
  }
}