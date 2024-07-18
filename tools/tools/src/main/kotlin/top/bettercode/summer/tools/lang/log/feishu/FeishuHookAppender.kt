package top.bettercode.summer.tools.lang.log.feishu

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import top.bettercode.summer.tools.lang.log.AlarmAppender
import top.bettercode.summer.tools.lang.util.IPAddressUtil

open class FeishuHookAppender(
    properties: FeishuProperties,
) : AlarmAppender<FeishuProperties>(properties) {

    private val log: Logger = LoggerFactory.getLogger(FeishuHookAppender::class.java)
    private val chatClient: FeishuHookClient =
        FeishuHookClient(properties.chatHook!!.webhook!!, properties.chatHook!!.secret)
    private val timeoutChatClient: FeishuHookClient?

    init {
        val timeoutChatHook = properties.timeoutChatHook
        timeoutChatClient = if (timeoutChatHook != null) {
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
        timeout: Boolean
    ): Boolean {
        val chat = if (timeout) timeoutChatClient ?: chatClient else chatClient
        return if (!IPAddressUtil.isPortConnectable(properties.managementHostName, properties.managementPort)) {
            chat.postMessage(
                title = properties.warnTitle,
                subTitle = properties.apiAddress,
                initialComment = initialComment,
                message = message.last()
            )
        } else {
            val (logUrl, linkTitle) = logUrl(properties.actuatorAddress, message)
            chat.postMessage(
                title = properties.warnTitle,
                subTitle = properties.apiAddress,
                initialComment = initialComment,
                logUrl = logUrl,
                linkTitle = linkTitle
            )
        }
    }

}