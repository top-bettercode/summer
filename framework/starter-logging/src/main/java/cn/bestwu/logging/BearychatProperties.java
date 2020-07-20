package cn.bestwu.logging;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * slack 配置
 *
 * @author Peter Wu
 */
@ConfigurationProperties("logging.bearychat")
public class BearychatProperties {

  private boolean sendFile = true;
  private String[] logger = {"root"};
  private String[] ignoredWarnLogger = {};
  private String webhookUrl;
  private String channel;
  private Long cacheSeconds = 5 * 60L;
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

  public String getWebhookUrl() {
    return webhookUrl;
  }

  public void setWebhookUrl(String webhookUrl) {
    this.webhookUrl = webhookUrl;
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

  public BearychatProperties setCacheSeconds(Long cacheSeconds) {
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