package top.bettercode.logging.slack

import top.bettercode.lang.util.StringUtil
import org.junit.jupiter.api.Test

/**
 * @author Peter Wu
 */
class SlackClientTest {

    private val slackClient = SlackClient("", "http://abc.com/")

    @Test
    fun errorToken() {
        println(StringUtil.valueOf(SlackClient("xoxb-", "").channelsList(), true))
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
        println(StringUtil.valueOf(slackClient.postMessage("dev", "test", "test", listOf("123testtest"), null), true))
    }

    @Test
    fun filesUpload() {
        println(StringUtil.valueOf(slackClient.filesUpload("dev", "test", "test", listOf("123testtest")), true))
    }
}