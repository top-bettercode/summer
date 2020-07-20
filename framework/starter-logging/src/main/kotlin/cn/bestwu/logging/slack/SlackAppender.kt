package cn.bestwu.logging.slack

import cn.bestwu.logging.RequestLoggingFilter
import cn.bestwu.logging.SlackProperties
import cn.bestwu.logging.format
import cn.bestwu.logging.logback.AlarmAppender
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.slf4j.MarkerFactory
import java.io.File

open class SlackAppender(private val properties: SlackProperties, private val title: String, private val logsPath: String?, val logUrl: String?) : AlarmAppender(properties.cyclicBufferSize, properties.cacheSeconds, properties.ignoredWarnLogger) {

    private val log: Logger = LoggerFactory.getLogger(SlackAppender::class.java)
    private val slackClient: SlackClient = SlackClient(properties.authToken, logUrl)

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

    override fun sendMessage(timeStamp: Long, initialComment: String, message: List<String>): Boolean {
        return try {
            val title = "$title ${format(timeStamp)}"
            slackClient.postMessage(properties.channel, title, initialComment, message, logsPath)
        } catch (e: Exception) {
            log.error(MarkerFactory.getMarker(RequestLoggingFilter.NO_ALARM_LOG_MARKER), "slack 发送信息失败", e)
            false
        }
    }

}