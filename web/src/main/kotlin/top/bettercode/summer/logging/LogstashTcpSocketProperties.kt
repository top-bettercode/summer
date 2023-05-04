package top.bettercode.summer.logging

import ch.qos.logback.classic.spi.ILoggingEvent
import ch.qos.logback.core.encoder.Encoder
import ch.qos.logback.core.util.Duration
import net.logstash.logback.appender.AbstractLogstashTcpSocketAppender
import net.logstash.logback.appender.AsyncDisruptorAppender
import net.logstash.logback.encoder.LogstashEncoder
import org.springframework.boot.context.properties.ConfigurationProperties

/**
 * RequestLogging 配置
 *
 * @author Peter Wu
 */
@ConfigurationProperties("summer.logging.logstash")
open class LogstashTcpSocketProperties {
    /**
     * The includeCallerData option takes a boolean value. If true, the caller data will be available
     * to the remote host. By default no caller data is sent to the server.
     */
    var isIncludeCallerData = false

    /**
     * Destinations to which to attempt to send logs, in order of preference.
     *
     *
     *
     *
     * Logs are only sent to one destination at a time.
     *
     *
     */
    var destinations: Array<String>?=null

    /**
     * Time period for which to wait after a connection fails, before attempting to reconnect.
     */
    var reconnectionDelay = Duration(AbstractLogstashTcpSocketAppender.DEFAULT_RECONNECTION_DELAY.toLong())

    /**
     * The encoder which is ultimately responsible for writing the event to the socket's
     * [java.io.OutputStream].
     */
    var encoderClass: Class<out Encoder<ILoggingEvent?>?> = LogstashEncoder::class.java

    /**
     * The number of bytes available in the write buffer. Defaults to DEFAULT_WRITE_BUFFER_SIZE
     *
     *
     * If less than or equal to zero, buffering the output stream will be disabled. If buffering is
     * disabled, the writer thread can slow down, but it will also can prevent dropping events in the
     * buffer on flaky connections.
     */
    var writeBufferSize = AbstractLogstashTcpSocketAppender.DEFAULT_WRITE_BUFFER_SIZE

    /**
     * If this duration elapses without an event being sent, then the keepAliveDuration will be sent
     * to the socket in order to keep the connection alive.
     *
     *
     * When null (the default), no keepAlive messages will be sent.
     */
    var keepAliveDuration: Duration? = null

    /**
     * The size of the [RingBuffer]. If the handler thread is not as fast as the producing
     * threads, then the [RingBuffer] will eventually fill up, at which point events will be
     * dropped.
     *
     *
     * Must be a positive power of 2.
     */
    var ringBufferSize = AsyncDisruptorAppender.DEFAULT_RING_BUFFER_SIZE
}