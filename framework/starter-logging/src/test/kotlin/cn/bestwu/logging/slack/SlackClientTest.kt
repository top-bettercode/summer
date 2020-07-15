package cn.bestwu.logging.slack

import cn.bestwu.lang.util.StringUtil
import org.junit.jupiter.api.Test

/**
 * @author Peter Wu
 */
class SlackClientTest {

    private val slackClient = SlackClient("")

    @Test
    fun errorToken() {
        println(StringUtil.valueOf(SlackClient("xoxb-").channelsList(), true))
    }

    @Test
    fun channelsList() {
        println(StringUtil.valueOf(slackClient.channelsList(), true))
    }

    @Test
    fun channelIdByName() {
        println(StringUtil.valueOf(slackClient.channelIdByName("logging-test"), true))
    }

    @Test
    fun channelInfo() {
        println(StringUtil.valueOf(slackClient.channelInfo("CE3DABTT8"), true))
    }

    @Test
    fun channelExist() {
        val channelExist = slackClient.channelExist("logging-test")
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
        println(StringUtil.valueOf(slackClient.postMessage("logging-test", "test"), true))
    }

    @Test
    fun filesUpload() {
        println(StringUtil.valueOf(slackClient.filesUpload("logging-test", "test-content".toByteArray(), "test-name", "text", "test-title", "test-comment"), true))
    }
}