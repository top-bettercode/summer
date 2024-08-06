package top.bettercode.summer.tools.lang.log.feishu

import ch.qos.logback.classic.Level
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import top.bettercode.summer.tools.lang.log.AlarmAppender
import top.bettercode.summer.tools.lang.log.feishu.FeishuClient.Companion.template

open class FeishuHookAppender @JvmOverloads constructor(
    properties: FeishuProperties = FeishuProperties(),
) : AlarmAppender<FeishuProperties>(properties) {

    private val log: Logger = LoggerFactory.getLogger(FeishuHookAppender::class.java)
    private val chatClient: FeishuHookClient by lazy {
        FeishuHookClient(this.properties.chatHook!!.webhook!!, this.properties.chatHook!!.secret)
    }

    private val timeoutChatClient: FeishuHookClient? by lazy {
        val timeoutChatHook = this.properties.timeoutChatHook
        if (timeoutChatHook != null) {
            val webhook = timeoutChatHook.webhook
            val secret = timeoutChatHook.secret
            if (webhook.isNullOrBlank()) {
                null
            } else
                FeishuHookClient(webhook, secret)
        } else {
            null
        }
    }

    override fun sendMessage(
        timeStamp: Long,
        initialComment: String,
        message: List<String>,
        level: Level,
        timeout: Boolean
    ): Boolean {
        val chat = if (timeout) timeoutChatClient ?: chatClient else chatClient
        val (logUrl, linkTitle) = logUrl(properties.actuatorAddress, message)
        return if (!isPortConnectable()) {
            chat.postMessage(
                title = properties.warnTitle,
                subTitle = properties.apiAddress,
                initialComment = initialComment,
                template = template(level),
                linkTitle = linkTitle,
                message = message.last()
            )
        } else {
            chat.postMessage(
                title = properties.warnTitle,
                subTitle = properties.apiAddress,
                initialComment = initialComment,
                template = template(level),
                logUrl = logUrl,
                linkTitle = linkTitle
            )
        }
    }

}