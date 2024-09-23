package top.bettercode.summer.tools.lang.log.feishu

import ch.qos.logback.classic.Level
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import top.bettercode.summer.tools.lang.log.AlarmAppender
import top.bettercode.summer.tools.lang.log.feishu.FeishuMsgClient.Companion.template
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ConcurrentMap

open class FeishuAppender @JvmOverloads constructor(
    properties: FeishuMsgProperties = FeishuMsgProperties(),
) : AlarmAppender<FeishuMsgProperties>(properties) {

    private val log: Logger = LoggerFactory.getLogger(FeishuAppender::class.java)
    private val client: FeishuMsgClient by lazy {
        FeishuMsgClient(
            this.properties.appId,
            this.properties.appSecret
        )
    }
    private var chatCache: ConcurrentMap<String, String?> = ConcurrentHashMap()

    private fun chatId(chat: String): String? {
        return chatCache.computeIfAbsent(chat) {
            try {
                client.chatIdByName(chat)
            } catch (e: Exception) {
                log.error(NO_ALARM_MARKER, "获取飞书群聊ID失败", e)
                null
            }
        }
    }

    override fun sendMessage(
        timeStamp: Long,
        initialComment: String,
        message: List<String>,
        level: Level,
        timeout: Boolean
    ): Boolean {
        val chat =
            if (timeout) properties.timeoutChat.ifBlank { properties.chat } else properties.chat
        val chatId = chatId(chat)
        return if (chatId != null) {
            val (logUrl, linkTitle) = logUrl(properties.actuatorAddress, message)
            if (!isPortConnectable()) {
                client.postMessage(
                    chatId = chatId,
                    title = properties.warnTitle,
                    subTitle = properties.apiAddress,
                    initialComment = initialComment,
                    template = template(level),
                    linkTitle = linkTitle,
                    message = message.last()
                )
            } else {
                client.postMessage(
                    chatId = chatId,
                    title = properties.warnTitle,
                    subTitle = properties.apiAddress,
                    initialComment = initialComment,
                    template = template(level),
                    logUrl = logUrl,
                    linkTitle = linkTitle
                )
            }
        } else {
            false
        }
    }

}