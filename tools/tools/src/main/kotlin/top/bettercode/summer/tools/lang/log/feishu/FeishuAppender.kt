package top.bettercode.summer.tools.lang.log.feishu

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.slf4j.MarkerFactory
import top.bettercode.summer.tools.lang.log.AlarmAppender
import top.bettercode.summer.tools.lang.util.IPAddressUtil

open class FeishuAppender(
    properties: FeishuProperties,
) : AlarmAppender<FeishuProperties>(properties) {

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
        val chat =
            if (timeout) properties.timeoutChat.ifBlank { properties.chat } else properties.chat
        val chatId = chatId(chat)
        return if (chatId != null) {
            try {
                if (!IPAddressUtil.isPortConnectable(properties.managementHostName, properties.managementPort)) {
                    client.filesUpload(
                        chatId = chatId,
                        timeStamp = timeStamp,
                        title =
                        "${properties.warnTitle}(${properties.apiAddress})".replace("/", "／"),
                        message = message
                    )
                } else {
                    val (logUrl, linkTitle) = logUrl(properties.actuatorAddress, message)
                    client.postMessage(
                        chatId = chatId,
                        title = properties.warnTitle,
                        subTitle = properties.apiAddress,
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