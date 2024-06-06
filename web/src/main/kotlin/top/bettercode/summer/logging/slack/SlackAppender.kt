package top.bettercode.summer.logging.slack

import ch.qos.logback.classic.encoder.PatternLayoutEncoder
import ch.qos.logback.core.util.OptionHelper
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.slf4j.MarkerFactory
import top.bettercode.summer.tools.lang.log.AlarmAppender
import java.util.concurrent.ConcurrentMap
import javax.net.ssl.SSLHandshakeException

open class SlackAppender(
    private val properties: top.bettercode.summer.logging.SlackProperties,
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

    private val log: Logger = LoggerFactory.getLogger(SlackAppender::class.java)
    private val slackClient: SlackClient =
        SlackClient(properties.authToken, logsPath, managementLogPath)
    private var channelExist: Boolean? = null

    private fun channelExist(): Boolean {
        if (channelExist == null) {
            try {
                channelExist = slackClient.channelExist(properties.channel)
            } catch (e: Exception) {
                log.error(
                    MarkerFactory.getMarker(NO_ALARM_LOG_MARKER),
                    "slack 查询频道信息失败",
                    e
                )
                if (e.cause is SSLHandshakeException) {
                    channelExist = false
                    log.warn(
                        MarkerFactory.getMarker(NO_ALARM_LOG_MARKER),
                        """Java 的信任库中没有包含所需的证书。解决这个问题的方法如下：
导入目标服务器的证书到 Java 信任库

首先，从目标服务器（https://slack.com）下载其 SSL 证书。你可以使用浏览器或者命令行工具来完成这个任务。然后使用 keytool 工具将证书导入到 Java 的信任库中。
步骤：

    获取证书：

    bash

echo | openssl s_client -connect slack.com:443 | openssl x509 > slack-com.crt

导入证书到 Java 信任库：

bash

keytool -import -alias slack-com -keystore ${'$'}JAVA_HOME/lib/security/cacerts -file slack-com.crt

默认密码是 changeit。
"""
                    )
                }
            }
        }
        return channelExist ?: false
    }

    override fun sendMessage(
        timeStamp: Long,
        initialComment: String,
        message: List<String>,
        timeout: Boolean
    ): Boolean {
        return if (channelExist()) {
            try {
                val title =
                    "$warnSubject${
                        try {
                            "(${top.bettercode.summer.logging.LoggingUtil.apiAddress})"
                        } catch (e: Exception) {
                            ""
                        }
                    }"
                slackClient.postMessage(
                    if (timeout) properties.timeoutChannel else properties.channel,
                    timeStamp,
                    title,
                    initialComment,
                    message
                )
            } catch (e: Exception) {
                log.error(
                    MarkerFactory.getMarker(NO_ALARM_LOG_MARKER),
                    "slack 发送信息失败",
                    e
                )
                false
            }
        } else {
            false
        }
    }

}