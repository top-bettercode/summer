package top.bettercode.summer.tools.lang.log.slack

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.slf4j.MarkerFactory
import top.bettercode.summer.tools.lang.log.AlarmAppender
import top.bettercode.summer.tools.lang.util.IPAddressUtil
import javax.net.ssl.SSLHandshakeException

open class SlackAppender(
    properties: SlackProperties,
) : AlarmAppender<SlackProperties>(properties) {

    private val log: Logger = LoggerFactory.getLogger(SlackAppender::class.java)
    private val client: SlackClient = SlackClient(properties.authToken)
    private var channelExist: Boolean? = null

    private fun channelExist(): Boolean {
        if (channelExist == null) {
            try {
                channelExist = client.channelExist(properties.channel)
            } catch (e: Exception) {
                if (e.cause is SSLHandshakeException) {
                    client.useCustomKeyStore = true
                    try {
                        channelExist = client.channelExist(properties.channel)
                    } catch (e: Exception) {
                        channelExist = false
                        log.error(
                            MarkerFactory.getMarker(NO_ALARM_LOG_MARKER),
                            "slack 查询频道信息失败",
                            e
                        )
                    }
                } else {
                    channelExist = false
                    log.error(
                        MarkerFactory.getMarker(NO_ALARM_LOG_MARKER),
                        "slack 查询频道信息失败",
                        e
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
                val channel = if (timeout) properties.timeoutChannel else properties.channel
                val title = "${properties.warnTitle}(${properties.apiAddress})"
                if (!IPAddressUtil.isPortConnectable(
                        properties.managementHostName,
                        properties.managementPort
                    )
                ) {
                    client.filesUpload(
                        channel = channel,
                        timeStamp = timeStamp,
                        title = title,
                        initialComment = initialComment,
                        message = message
                    )
                } else {
                    val (logUrl, linkTitle) = logUrl(properties.actuatorAddress, message)
                    client.postMessage(
                        channel = channel,
                        title = title,
                        initialComment = initialComment,
                        logUrl = logUrl,
                        linkTitle = linkTitle
                    )
                }
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