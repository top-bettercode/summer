package top.bettercode.logging.slack

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.slf4j.MarkerFactory
import top.bettercode.lang.util.LocalDateTimeHelper
import top.bettercode.logging.RequestLoggingFilter
import top.bettercode.logging.SlackProperties
import top.bettercode.logging.logback.AlarmAppender
import java.io.File

open class SlackAppender(
    private val properties: SlackProperties,
    private val warnSubject: String,
    private val managementPath: String,
    private val logsPath: String?,
    logAll: Boolean
) : AlarmAppender(
    properties.cyclicBufferSize,
    properties.cacheSeconds,
    properties.ignoredWarnLogger,
    logAll
) {

    private val log: Logger = LoggerFactory.getLogger(SlackAppender::class.java)
    private val slackClient: SlackClient = SlackClient(properties.authToken, logAll, managementPath)

    override fun start() {
        if (slackClient.channelExist(properties.channel)) {
            super.start()
            if (!logsPath.isNullOrBlank()) {
                val file = File(logsPath, "alarm")
                if (!file.exists()) {
                    file.mkdirs()
                }
            }
        }
    }

    override fun sendMessage(
        timeStamp: Long,
        initialComment: String,
        message: List<String>
    ): Boolean {
        return try {
            val title =
                "$warnSubject${if (!RequestLoggingFilter.API_HOST.isNullOrBlank()) "(${RequestLoggingFilter.API_HOST})" else ""} ${
                    LocalDateTimeHelper.format(timeStamp)
                }"
            slackClient.postMessage(properties.channel, title, initialComment, message, logsPath)
        } catch (e: Exception) {
            log.error(
                MarkerFactory.getMarker(RequestLoggingFilter.NO_ALARM_LOG_MARKER),
                "slack 发送信息失败",
                e
            )
            false
        }
    }

}