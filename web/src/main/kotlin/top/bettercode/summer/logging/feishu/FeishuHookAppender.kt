package top.bettercode.summer.logging.feishu

import ch.qos.logback.classic.encoder.PatternLayoutEncoder
import ch.qos.logback.core.util.OptionHelper
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import top.bettercode.summer.logging.LoggingUtil
import top.bettercode.summer.tools.lang.log.AlarmAppender
import java.util.concurrent.ConcurrentMap

open class FeishuHookAppender(
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

    private val log: Logger = LoggerFactory.getLogger(FeishuHookAppender::class.java)
    private val chatClient: FeishuHookClient =
        FeishuHookClient(properties.chatHook!!.webhook!!, properties.chatHook!!.secret)
    private val timeoutChatClient: FeishuHookClient?

    init {
        val timeoutChatHook = properties.timeoutChatHook
        timeoutChatClient = if (timeoutChatHook != null) {
            val webhook = timeoutChatHook.webhook
            val secret = timeoutChatHook.secret
            if (webhook == null) {
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
        return if (actuatorAddress == null) {
            chat.postMessage(
                title = warnSubject,
                subTitle = subTitle,
                initialComment = initialComment,
                message = message.last()
            )
        } else {
            val (logUrl, linkTitle) = logUrl(actuatorAddress, message)
            chat.postMessage(
                title = warnSubject,
                subTitle = subTitle,
                initialComment = initialComment,
                logUrl = logUrl,
                linkTitle = linkTitle
            )
        }
    }

}