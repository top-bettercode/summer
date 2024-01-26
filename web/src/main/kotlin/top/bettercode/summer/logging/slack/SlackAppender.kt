package top.bettercode.summer.logging.slack

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.slf4j.MarkerFactory
import top.bettercode.summer.tools.lang.log.AlarmAppender
import java.util.concurrent.ConcurrentMap

open class SlackAppender(
        private val properties: top.bettercode.summer.logging.SlackProperties,
        private val warnSubject: String,
        logsPath: String,
        managementLogPath: String,
        logPattern: String,
        cacheMap: ConcurrentMap<String, Int>,
        timeoutCacheMap: ConcurrentMap<String, Int>
) : AlarmAppender(
        properties.cyclicBufferSize,
        properties.ignoredWarnLogger,
        logPattern,
        cacheMap,
        timeoutCacheMap
) {

    private val log: Logger = LoggerFactory.getLogger(SlackAppender::class.java)
    private val slackClient: SlackClient = SlackClient(properties.authToken, logsPath, managementLogPath)

    override fun start() {
        if (slackClient.channelExist(properties.channel)) {
            super.start()
        }
    }

    override fun sendMessage(
            timeStamp: Long,
            initialComment: String,
            message: List<String>,
            timeout: Boolean
    ): Boolean {
        return try {
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
    }

}