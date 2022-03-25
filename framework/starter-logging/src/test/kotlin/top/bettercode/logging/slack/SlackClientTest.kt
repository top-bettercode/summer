package top.bettercode.logging.slack

import org.junit.jupiter.api.Test
import top.bettercode.lang.util.StringUtil
import top.bettercode.logging.operation.RequestConverter

/**
 * @author Peter Wu
 */
class SlackClientTest {

    private val slackClient = SlackClient("", true, "/actuator")

    @Test
    fun apiHost() {
        try {
            System.err.println(RequestConverter.apiHost)
        } catch (e: Exception) {
            e.printStackTrace()
            System.err.println("------------------------------------")
            System.err.println(RequestConverter.apiHost)
        }
    }

    @Test
    fun errorToken() {
        println(StringUtil.valueOf(SlackClient("xoxb-", true, "/actuator").channelsList(), true))
    }

    @Test
    fun channelsList() {
        println(StringUtil.valueOf(slackClient.channelsList(), true))
    }

    @Test
    fun channelIdByName() {
        println(StringUtil.valueOf(slackClient.channelIdByName("test"), true))
    }

    @Test
    fun channelExist() {
        val channelExist = slackClient.channelExist("dev")
        org.junit.jupiter.api.Assertions.assertTrue(channelExist)
        println(StringUtil.valueOf(channelExist, true))
    }

    @Test
    fun channelNotExist() {
        val channelExist = slackClient.channelExist("logging")
        org.junit.jupiter.api.Assertions.assertFalse(channelExist)
        println(StringUtil.valueOf(channelExist, true))
    }

    @Test
    fun postMessage() {
        println(
            StringUtil.valueOf(
                slackClient.postMessage(
                    "dev",
                    System.currentTimeMillis(),
                    "test",
                    "test",
                    listOf("123testtest"),
                    "build"
                ), true
            )
        )
    }

    @Test
    fun filesUpload() {
        println(
            StringUtil.valueOf(
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