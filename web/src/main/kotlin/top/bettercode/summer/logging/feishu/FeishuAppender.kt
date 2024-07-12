package top.bettercode.summer.logging.feishu

import ch.qos.logback.classic.encoder.PatternLayoutEncoder
import ch.qos.logback.core.util.OptionHelper
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.slf4j.MarkerFactory
import top.bettercode.summer.logging.LoggingUtil
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

    private val log: Logger = LoggerFactory.getLogger(FeishuAppender::class.java)
    private val client: FeishuClient = FeishuClient(properties.appId, properties.appSecret)
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
                val actuatorAddress = try {
                    LoggingUtil.actuatorAddress
                } catch (e: Exception) {
                    null
                }
                val subTitle = try {
                    LoggingUtil.apiAddress
                } catch (e: Exception) {
                    ""
                }
                if (actuatorAddress == null) {
                    client.filesUpload(
                        chatId = chatId,
                        timeStamp = timeStamp,
                        title = warnSubject + subTitle,
                        message = message
                    )
                } else {
                    val (logUrl, linkTitle) = logUrl(actuatorAddress, message)
                    client.postMessage(
                        chatId = chatId,
                        title = warnSubject,
                        subTitle = subTitle,
                        initialComment = initialComment,
                        logUrl = logUrl,
                        linkTitle = linkTitle
                    )
                }
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