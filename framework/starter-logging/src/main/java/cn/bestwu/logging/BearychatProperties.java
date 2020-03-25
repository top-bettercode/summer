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
  private String logUrl;
  private String channel;
  private Integer sendDelaySeconds = 5;
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

  public String getLogUrl() {
    return logUrl;
  }

  public void setLogUrl(String logUrl) {
    this.logUrl = logUrl;
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