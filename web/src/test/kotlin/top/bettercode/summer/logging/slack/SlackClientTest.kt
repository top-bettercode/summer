package top.bettercode.summer.logging.slack

import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import top.bettercode.summer.tools.lang.log.slack.SlackClient
import top.bettercode.summer.tools.lang.util.StringUtil
import javax.net.ssl.SSLHandshakeException

/**
 * @author Peter Wu
 */
@Disabled
class SlackClientTest {

    private val client = SlackClient("")

    @Test
    fun errorToken() {
        println(StringUtil.json(SlackClient("xoxb-").channelsList(), true))
    }

    @Test
    fun channelsList() {
        val channelsList = try {
            client.channelsList()
        } catch (e: Exception) {
            if (e.cause is SSLHandshakeException) {
                client.useCustomKeyStore = true
                client.channelsList()
            } else throw e
        }
        println(StringUtil.json(channelsList, true))
    }

    @Test
    fun channelIdByName() {
        println(StringUtil.json(client.channelIdByName("test"), true))
    }

    @Test
    fun channelExist() {
        val channelExist = client.channelExist("dev")
        println(StringUtil.json(channelExist, true))
    }

    @Test
    fun channelNotExist() {
        val channelExist = client.channelExist("logging")
        org.junit.jupiter.api.Assertions.assertFalse(channelExist)
        println(StringUtil.json(channelExist, true))
    }

    @Test
    fun postMessage() {
        println(
            StringUtil.json(
                client.postMessage(
                    channel = "dev",
                    title = "title",
                    initialComment = "initialComment",
                    logUrl = "http://localhost:8080/actuator/logs",
                    linkTitle = "logging"
                ), true
            )
        )
    }

    @Test
    fun filesUpload() {
        println(
            StringUtil.json(
                client.filesUpload(
                    channel = "dev",
                    timeStamp = System.currentTimeMillis(),
                    title = "title",
                    initialComment = "initialComment",
                    message = listOf("message")
                ), true
            )
        )
    }
}