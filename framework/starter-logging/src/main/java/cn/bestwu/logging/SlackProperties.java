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
  private Long cacheSeconds = 10L;
  private Integer cyclicBufferSize = 20;

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

  public Long getCacheSeconds() {
    return cacheSeconds;
  }

  public SlackProperties setCacheSeconds(Long cacheSeconds) {
    this.cacheSeconds = cacheSeconds;
    return this;
  }

  public Integer getCyclicBufferSize() {
    return cyclicBufferSize;
  }

  public void setCyclicBufferSize(Integer cyclicBufferSize) {
    this.cyclicBufferSize = cyclicBufferSize;
  }
}