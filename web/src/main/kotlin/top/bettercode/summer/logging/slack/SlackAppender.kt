package top.bettercode.summer.logging.slack

import ch.qos.logback.classic.encoder.PatternLayoutEncoder
import ch.qos.logback.core.util.OptionHelper
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.slf4j.MarkerFactory
import top.bettercode.summer.logging.LoggingUtil
import top.bettercode.summer.tools.lang.log.AlarmAppender
import java.util.concurrent.ConcurrentMap
import javax.net.ssl.SSLHandshakeException

open class SlackAppender(
    private val properties: SlackProperties,
    private val warnSubject: String,
    logsPath: String,
    managementLogPath: String,
    logPattern: String,
    cacheMap: ConcurrentMap<String, Int>,
    timeoutCacheMap: ConcurrentMap<String, Int>
) : AlarmAppender(
    logsPath = logsPath,
    managementLogPath = managementLogPath,
    cyclicBufferSize = properties.cyclicBufferSize,
    ignoredWarnLogger = properties.ignoredWarnLogger,
    encoder = PatternLayoutEncoder().apply {
        pattern = OptionHelper.substVars(logPattern, context)
    },
    cacheMap = cacheMap,
    timeoutCacheMap = timeoutCacheMap
) {

    private val log: Logger = LoggerFactory.getLogger(SlackAppender::class.java)
    private val client: SlackClient = SlackClient(properties.authToken)
    private var channelExist: Boolean? = null

    private fun channelExist(): Boolean {
        if (channelExist == null) {
            try {
                channelExist = client.channelExist(properties.channel)
            } catch (e: Exception) {
                if (e.cause is SSLHandshakeException) {
                    client.useCustomKeyStore = true
                    try {
                        channelExist = client.channelExist(properties.channel)
                    } catch (e: Exception) {
                        channelExist = false
                        log.error(
                            MarkerFactory.getMarker(NO_ALARM_LOG_MARKER),
                            "slack 查询频道信息失败",
                            e
                        )
                    }
                } else {
                    channelExist = false
                    log.error(
                        MarkerFactory.getMarker(NO_ALARM_LOG_MARKER),
                        "slack 查询频道信息失败",
                        e
                    )
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
                val actuatorAddress = try {
                    LoggingUtil.actuatorAddress
                } catch (e: Exception) {
                    null
                }
                val title = "$warnSubject${
                    try {
                        "(${LoggingUtil.apiAddress})"
                    } catch (e: Exception) {
                        ""
                    }
                }"
                val channel = if (timeout) properties.timeoutChannel else properties.channel
                if (actuatorAddress == null) {
                    client.filesUpload(
                        channel = channel,
                        timeStamp = timeStamp,
                        title = title,
                        initialComment = initialComment,
                        message = message
                    )
                } else {
                    val (logUrl, linkTitle) = logUrl(actuatorAddress, message)
                    client.postMessage(
                        channel = channel,
                        title = title,
                        initialComment = initialComment,
                        logUrl = logUrl,
                        linkTitle = linkTitle
                    )
                }
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