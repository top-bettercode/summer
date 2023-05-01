package top.bettercode.summer.logging;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * slack 配置
 *
 * @author Peter Wu
 */
@ConfigurationProperties("summer.logging.slack")
public class SlackProperties {

  private String[] logger = new String[]{"root"};
  private String[] ignoredWarnLogger = new String[0];
  private String authToken;
  private String channel;
  private String timeoutChannel = "timeout";
  private Long cacheSeconds = 5 * 60L;
  private Long timeoutCacheSeconds = 2 * 60 * 60L;
  private Integer cyclicBufferSize = 20;

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

  public String getTimeoutChannel() {
    return timeoutChannel;
  }

  public void setTimeoutChannel(String timeoutChannel) {
    this.timeoutChannel = timeoutChannel;
  }

  public Long getCacheSeconds() {
    return cacheSeconds;
  }

  public SlackProperties setCacheSeconds(Long cacheSeconds) {
    this.cacheSeconds = cacheSeconds;
    return this;
  }

  public Long getTimeoutCacheSeconds() {
    return timeoutCacheSeconds;
  }

  public void setTimeoutCacheSeconds(Long timeoutCacheSeconds) {
    this.timeoutCacheSeconds = timeoutCacheSeconds;
  }

  public Integer getCyclicBufferSize() {
    return cyclicBufferSize;
  }

  public void setCyclicBufferSize(Integer cyclicBufferSize) {
    this.cyclicBufferSize = cyclicBufferSize;
  }
}