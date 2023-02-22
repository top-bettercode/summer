package top.bettercode.summer.logging.slack

import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import top.bettercode.summer.tools.lang.util.StringUtil

/**
 * @author Peter Wu
 */
@Disabled
class SlackClientTest {

    private val slackClient = SlackClient("",  "/actuator")

    @Test
    fun errorToken() {
        println(StringUtil.valueOf(SlackClient("xoxb-",  "/actuator").channelsList(), true))
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