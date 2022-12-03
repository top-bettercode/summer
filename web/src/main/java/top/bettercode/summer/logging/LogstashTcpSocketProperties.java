package top.bettercode.summer.logging;

import static net.logstash.logback.appender.AbstractLogstashTcpSocketAppender.DEFAULT_RECONNECTION_DELAY;
import static net.logstash.logback.appender.AbstractLogstashTcpSocketAppender.DEFAULT_WRITE_BUFFER_SIZE;
import static net.logstash.logback.appender.AsyncDisruptorAppender.DEFAULT_RING_BUFFER_SIZE;

import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.encoder.Encoder;
import ch.qos.logback.core.util.Duration;
import net.logstash.logback.encoder.LogstashEncoder;
import net.logstash.logback.encoder.com.lmax.disruptor.RingBuffer;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * RequestLogging 配置
 *
 * @author Peter Wu
 */
@ConfigurationProperties("summer.logging.logstash")
public class LogstashTcpSocketProperties {

  /**
   * The includeCallerData option takes a boolean value. If true, the caller data will be available
   * to the remote host. By default no caller data is sent to the server.
   */
  private boolean includeCallerData = false;

  /**
   * Destinations to which to attempt to send logs, in order of preference.
   * <p>
   *
   * Logs are only sent to one destination at a time.
   * <p>
   */
  private String[] destinations;

  /**
   * Time period for which to wait after a connection fails, before attempting to reconnect.
   */
  private Duration reconnectionDelay = new Duration(DEFAULT_RECONNECTION_DELAY);
  /**
   * The encoder which is ultimately responsible for writing the event to the socket's {@link
   * java.io.OutputStream}.
   */
  private Class<? extends Encoder<ILoggingEvent>> encoderClass = LogstashEncoder.class;

  /**
   * The number of bytes available in the write buffer. Defaults to DEFAULT_WRITE_BUFFER_SIZE
   * <p>
   * If less than or equal to zero, buffering the output stream will be disabled. If buffering is
   * disabled, the writer thread can slow down, but it will also can prevent dropping events in the
   * buffer on flaky connections.
   */
  private int writeBufferSize = DEFAULT_WRITE_BUFFER_SIZE;
  /**
   * If this duration elapses without an event being sent, then the keepAliveDuration will be sent
   * to the socket in order to keep the connection alive.
   * <p>
   * When null (the default), no keepAlive messages will be sent.
   */
  private Duration keepAliveDuration;
  /**
   * The size of the {@link RingBuffer}. If the handler thread is not as fast as the producing
   * threads, then the {@link RingBuffer} will eventually fill up, at which point events will be
   * dropped.
   * <p>
   * Must be a positive power of 2.
   */
  private int ringBufferSize = DEFAULT_RING_BUFFER_SIZE;

  public boolean isIncludeCallerData() {
    return includeCallerData;
  }

  public void setIncludeCallerData(boolean includeCallerData) {
    this.includeCallerData = includeCallerData;
  }

  public String[] getDestinations() {
    return destinations;
  }

  public void setDestinations(String[] destinations) {
    this.destinations = destinations;
  }

  public Duration getReconnectionDelay() {
    return reconnectionDelay;
  }

  public void setReconnectionDelay(Duration reconnectionDelay) {
    this.reconnectionDelay = reconnectionDelay;
  }

  public Class<? extends Encoder<ILoggingEvent>> getEncoderClass() {
    return encoderClass;
  }

  public void setEncoderClass(
      Class<? extends Encoder<ILoggingEvent>> encoderClass) {
    this.encoderClass = encoderClass;
  }

  public int getWriteBufferSize() {
    return writeBufferSize;
  }

  public void setWriteBufferSize(int writeBufferSize) {
    this.writeBufferSize = writeBufferSize;
  }

  public Duration getKeepAliveDuration() {
    return keepAliveDuration;
  }

  public void setKeepAliveDuration(Duration keepAliveDuration) {
    this.keepAliveDuration = keepAliveDuration;
  }

  public int getRingBufferSize() {
    return ringBufferSize;
  }

  public void setRingBufferSize(int ringBufferSize) {
    this.ringBufferSize = ringBufferSize;
  }
}