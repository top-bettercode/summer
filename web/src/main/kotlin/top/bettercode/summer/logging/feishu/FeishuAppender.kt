package top.bettercode.summer.logging.feishu

import ch.qos.logback.classic.encoder.PatternLayoutEncoder
import ch.qos.logback.core.util.OptionHelper
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.slf4j.MarkerFactory
import top.bettercode.summer.tools.lang.log.AlarmAppender
import java.util.concurrent.ConcurrentMap

open class FeishuAppender(
    private val properties: FeishuProperties,
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

    private val log: Logger = LoggerFactory.getLogger(FeishuAppender::class.java)
    private val client: FeishuClient =
        FeishuClient(properties.appId, properties.appSecret, logsPath, managementLogPath)
    private var chatCache: MutableMap<String, String?> = mutableMapOf()

    private fun chatId(chat: String): String? {
        return chatCache.computeIfAbsent(chat) {
            try {
                client.chatIdByName(chat)
            } catch (e: Exception) {
                log.error(
                    MarkerFactory.getMarker(NO_ALARM_LOG_MARKER),
                    "获取飞书群聊ID失败",
                    e
                )
                null
            }
        }
    }

    override fun sendMessage(
        timeStamp: Long,
        initialComment: String,
        message: List<String>,
        timeout: Boolean
    ): Boolean {
        val chat = if (timeout) properties.timeoutChat else properties.chat
        val chatId = chatId(chat)
        return if (chatId != null) {
            try {
                val title =
                    "$warnSubject${
                        try {
                            "(${top.bettercode.summer.logging.LoggingUtil.apiAddress})"
                        } catch (e: Exception) {
                            ""
                        }
                    }"
                client.postMessage(
                    chatId,
                    timeStamp,
                    title,
                    initialComment,
                    message
                )
            } catch (e: Exception) {
                log.error(
                    MarkerFactory.getMarker(NO_ALARM_LOG_MARKER),
                    "feishu 发送信息失败",
                    e
                )
                false
            }
        } else {
            false
        }
    }

}