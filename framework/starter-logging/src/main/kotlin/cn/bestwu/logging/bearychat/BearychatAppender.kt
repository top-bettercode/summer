package cn.bestwu.logging.bearychat

import cn.bestwu.logging.BearychatProperties
import cn.bestwu.logging.RequestLoggingFilter
import cn.bestwu.logging.format
import cn.bestwu.logging.logback.AlarmAppender
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.slf4j.MarkerFactory
import java.io.File


open class BearychatAppender(private val properties: BearychatProperties, private val title: String, private val logsPath: String?, val logUrl: String?) : AlarmAppender(properties.cyclicBufferSize, properties.cacheSeconds, properties.ignoredWarnLogger) {


    private val log: Logger = LoggerFactory.getLogger(BearychatAppender::class.java)
    private val client: BearychatClient = BearychatClient(properties.webhookUrl, logUrl)

    init {
        if (!logsPath.isNullOrBlank()) {
            val file = File(logsPath, "alarm")
            if (!file.exists()) {
                file.mkdirs()
            }
        }
    }


    override fun sendMessage(timeStamp: Long, initialComment: String, message: List<String>): Boolean {
        return try {
            val title = "$title ${format(timeStamp)}"
            client.postMessage(properties.channel, title, initialComment, message, logsPath)
        } catch (e: Exception) {
            log.error(MarkerFactory.getMarker(RequestLoggingFilter.NO_ALARM_LOG_MARKER), "bearychat 发送信息失败", e)
            false
        }
    }


}