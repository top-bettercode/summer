package top.bettercode.summer.tools.lang.log.slack

import ch.qos.logback.classic.Level
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import top.bettercode.summer.tools.lang.log.AlarmAppender
import top.bettercode.summer.tools.lang.log.AlarmMarker
import javax.net.ssl.SSLHandshakeException

open class SlackAppender @JvmOverloads constructor(
    properties: SlackProperties = SlackProperties(),
) : AlarmAppender<SlackProperties>(properties) {

    private val log: Logger = LoggerFactory.getLogger(SlackAppender::class.java)
    private val client: SlackClient by lazy {
        SlackClient(this.properties.authToken)
    }
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
                        log.error(AlarmMarker.noAlarmMarker, "slack 查询频道信息失败", e)
                    }
                } else {
                    channelExist = false
                    log.error(AlarmMarker.noAlarmMarker, "slack 查询频道信息失败", e)
                }
            }
        }
        return channelExist ?: false
    }

    override fun sendMessage(
        timeStamp: Long,
        initialComment: String,
        message: List<String>,
        level: Level,
        timeout: Boolean
    ): Boolean {
        return if (channelExist()) {
            val channel = if (timeout) properties.timeoutChannel else properties.channel
            val title = "${properties.warnTitle}(${properties.apiAddress})"
            if (!isPortConnectable()) {
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
        } else {
            false
        }
    }

}