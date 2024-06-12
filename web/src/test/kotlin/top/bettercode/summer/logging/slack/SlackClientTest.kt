package top.bettercode.summer.logging.slack

import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import top.bettercode.summer.tools.lang.util.StringUtil
import javax.net.ssl.SSLHandshakeException

/**
 * @author Peter Wu
 */
@Disabled
class SlackClientTest {

    private val slackClient = SlackClient(
        "",
        "./build",
        "/actuator"
    )

    @Test
    fun errorToken() {
        println(StringUtil.json(SlackClient("xoxb-", "./build", "/actuator").channelsList(), true))
    }

    @Test
    fun channelsList() {
        val channelsList = try {
            slackClient.channelsList()
        } catch (e: Exception) {
            if (e.cause is SSLHandshakeException) {
                slackClient.useCustomKeyStore = true
                slackClient.channelsList()
            } else throw e
        }
        println(StringUtil.json(channelsList, true))
    }

    @Test
    fun channelIdByName() {
        println(StringUtil.json(slackClient.channelIdByName("test"), true))
    }

    @Test
    fun channelExist() {
        val channelExist = slackClient.channelExist("dev")
        println(StringUtil.json(channelExist, true))
    }

    @Test
    fun channelNotExist() {
        val channelExist = slackClient.channelExist("logging")
        org.junit.jupiter.api.Assertions.assertFalse(channelExist)
        println(StringUtil.json(channelExist, true))
    }

    @Test
    fun postMessage() {
        println(
            StringUtil.json(
                slackClient.postMessage(
                    "dev",
                    System.currentTimeMillis(),
                    "test",
                    "test",
                    listOf("123testtest")
                ), true
            )
        )
    }

    @Test
    fun filesUpload() {
        println(
            StringUtil.json(
                slackClient.filesUpload(
                    "dev",
                    System.currentTimeMillis(),
                    "title",
                    "initialComment",
                    listOf("message")
                ), true
            )
        )
    }
}