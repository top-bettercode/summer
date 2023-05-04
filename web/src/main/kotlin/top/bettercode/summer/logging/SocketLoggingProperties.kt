package top.bettercode.summer.logging

import ch.qos.logback.core.net.ssl.SSLConfiguration
import org.springframework.boot.context.properties.ConfigurationProperties
import java.time.Duration

/**
 * RequestLogging 配置
 *
 * @author Peter Wu
 */
@ConfigurationProperties("summer.logging.socket")
open class SocketLoggingProperties {
    /**
     * The includeCallerData option takes a boolean value. If true, the caller data will be available
     * to the remote host. By default no caller data is sent to the server.
     */
    var includeCallerData = false

    /**
     * The port number of the remote server.
     */
    var port = 4560

    /**
     * The reconnectionDelay option takes a duration string, such "10 seconds" representing the time
     * to wait between each failed connection attempt to the server. The default value of this option
     * is 30 seconds. Setting this option to zero turns off reconnection capability. Note that in case
     * of successful connection to the server, there will be no connector thread present.
     */
    var reconnectionDelay: Duration = Duration.ofSeconds(30)

    /**
     * The queueSize property takes an integer (greater than zero) representing the number of logging
     * events to retain for delivery to the remote receiver. When the queue size is one, event
     * delivery to the remote receiver is synchronous. When the queue size is greater than one, new
     * events are enqueued, assuming that there is space available in the queue. Using a queue length
     * greater than one can improve performance by eliminating delays caused by transient network
     * delays.
     *
     *
     * See also the eventDelayLimit property.
     */
    var queueSize = 128

    /**
     * The eventDelayLimit option takes a duration string, such "10 seconds". It represents the time
     * to wait before dropping events in case the local queue is full, i.e. already contains queueSize
     * events. This may occur if the remote host is persistently slow accepting events. The default
     * value of this option is 100 milliseconds.
     */
    var eventDelayLimit: Duration = Duration.ofMillis(100)

    /**
     * The host name of the server.
     */
    var remoteHost: String? = null

    /**
     * Supported only for SSLSocketAppender, this property provides the SSL configuration that will be
     * used by the appender, as described in Using SSL.
     */
    var ssl: SSLConfiguration? = null
}