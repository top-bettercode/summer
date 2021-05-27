package cn.bestwu.logging;

import ch.qos.logback.core.net.ssl.SSLConfiguration;
import java.time.Duration;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * RequestLogging 配置
 *
 * @author Peter Wu
 */
@ConfigurationProperties("summer.logging.socket")
public class SocketLoggingProperties {

  /**
   * The includeCallerData option takes a boolean value. If true, the caller data will be available
   * to the remote host. By default no caller data is sent to the server.
   */
  private boolean includeCallerData = false;

  /**
   * The port number of the remote server.
   */
  private Integer port = 4560;
  /**
   * The reconnectionDelay option takes a duration string, such "10
   * seconds" representing the time to wait between each failed connection attempt to the server.
   * The default value of this option is 30 seconds. Setting this option to zero turns off
   * reconnection capability. Note that in case of successful connection to the server, there will
   * be no connector thread present.
   */
  private Duration reconnectionDelay = Duration.ofSeconds(30);

  /**
   * The queueSize property takes an integer (greater than zero) representing the number of logging
   * events to retain for delivery to the remote receiver. When the queue size is one, event
   * delivery to the remote receiver is synchronous. When the queue size is greater than one, new
   * events are enqueued, assuming that there is space available in the queue. Using a queue length
   * greater than one can improve performance by eliminating delays caused by transient network
   * delays.
   *
   * See also the eventDelayLimit property.
   */
  private Integer queueSize = 128;
  /**
   * The eventDelayLimit option takes a duration string, such "10 seconds". It represents the time
   * to wait before dropping events in case the local queue is full, i.e. already contains queueSize
   * events. This may occur if the remote host is persistently slow accepting events. The default
   * value of this option is 100 milliseconds.
   */
  private Duration eventDelayLimit = Duration.ofMillis(100);

  /**
   * The host name of the server.
   */
  private String remoteHost;

  /**
   * Supported only for SSLSocketAppender, this property provides the SSL configuration that will be
   * used by the appender, as described in Using SSL.
   */
  private SSLConfiguration ssl;

  public boolean isIncludeCallerData() {
    return includeCallerData;
  }

  public void setIncludeCallerData(boolean includeCallerData) {
    this.includeCallerData = includeCallerData;
  }

  public Integer getPort() {
    return port;
  }

  public void setPort(Integer port) {
    this.port = port;
  }

  public Duration getReconnectionDelay() {
    return reconnectionDelay;
  }

  public void setReconnectionDelay(Duration reconnectionDelay) {
    this.reconnectionDelay = reconnectionDelay;
  }

  public Integer getQueueSize() {
    return queueSize;
  }

  public void setQueueSize(Integer queueSize) {
    this.queueSize = queueSize;
  }

  public Duration getEventDelayLimit() {
    return eventDelayLimit;
  }

  public void setEventDelayLimit(Duration eventDelayLimit) {
    this.eventDelayLimit = eventDelayLimit;
  }

  public String getRemoteHost() {
    return remoteHost;
  }

  public void setRemoteHost(String remoteHost) {
    this.remoteHost = remoteHost;
  }

  public SSLConfiguration getSsl() {
    return ssl;
  }

  public void setSsl(SSLConfiguration ssl) {
    this.ssl = ssl;
  }
}