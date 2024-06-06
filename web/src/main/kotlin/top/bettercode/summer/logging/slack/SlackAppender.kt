package top.bettercode.summer.logging.slack

import ch.qos.logback.classic.encoder.PatternLayoutEncoder
import ch.qos.logback.core.util.OptionHelper
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.slf4j.MarkerFactory
import top.bettercode.summer.tools.lang.log.AlarmAppender
import java.util.concurrent.ConcurrentMap
import javax.net.ssl.SSLHandshakeException

open class SlackAppender(
    private val properties: top.bettercode.summer.logging.SlackProperties,
    private val warnSubject: String,
    logsPath: String,
    managementLogPath: String,
    logPattern: String,
    cacheMap: ConcurrentMap<String, Int>,
    timeoutCacheMap: ConcurrentMap<String, Int>
) : AlarmAppender(
    cyclicBufferSize = properties.cyclicBufferSize,
    ignoredWarnLogger = properties.ignoredWarnLogger,
    encoder = PatternLayoutEncoder().apply {
        pattern = OptionHelper.substVars(logPattern, context)
    },
    cacheMap = cacheMap,
    timeoutCacheMap = timeoutCacheMap
) {

    private val log: Logger = LoggerFactory.getLogger(SlackAppender::class.java)
    private val slackClient: SlackClient =
        SlackClient(properties.authToken, logsPath, managementLogPath)
    private var channelExist: Boolean? = null

    private fun channelExist(): Boolean {
        if (channelExist == null) {
            try {
                channelExist = slackClient.channelExist(properties.channel)
            } catch (e: Exception) {
                log.error(
                    MarkerFactory.getMarker(NO_ALARM_LOG_MARKER),
                    "slack 查询频道信息失败",
                    e
                )
                if (e.cause is SSLHandshakeException) {
                    channelExist = false
                }
            }
        }
        return channelExist ?: false
    }

    override fun sendMessage(
        timeStamp: Long,
        initialComment: String,
        message: List<String>,
        timeout: Boolean
    ): Boolean {
        return if (channelExist()) {
            try {
                val title =
                    "$warnSubject${
                        try {
                            "(${top.bettercode.summer.logging.LoggingUtil.apiAddress})"
                        } catch (e: Exception) {
                            ""
                        }
                    }"
                slackClient.postMessage(
                    if (timeout) properties.timeoutChannel else properties.channel,
                    timeStamp,
                    title,
                    initialComment,
                    message
                )
            } catch (e: Exception) {
                log.error(
                    MarkerFactory.getMarker(NO_ALARM_LOG_MARKER),
                    "slack 发送信息失败",
                    e
                )
                false
            }
        } else {
            false
        }
    }

}