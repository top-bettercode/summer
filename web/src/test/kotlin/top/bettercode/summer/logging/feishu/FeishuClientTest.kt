package top.bettercode.summer.logging.feishu

import org.junit.jupiter.api.Test
import top.bettercode.summer.tools.lang.log.feishu.FeishuClient
import top.bettercode.summer.tools.lang.util.StringUtil

/**
 *
 * @author Peter Wu
 */
class FeishuClientTest {

    val client = FeishuClient(
        "",
        ""
    )

    @Test
    fun chatList() {
        val channelsList = client.chatList()
        println(StringUtil.json(channelsList, true))
    }

    @Test
    fun chatIdByName() {
        println(StringUtil.json(client.chatIdByName("dev"), true))
    }

    @Test
    fun chatExist() {
        val chatExist = client.chatExist("dev")
        println(StringUtil.json(chatExist, true))
    }

    @Test
    fun chatNotExist() {
        val chatExist = client.chatExist("logging")
        org.junit.jupiter.api.Assertions.assertFalse(chatExist)
        println(StringUtil.json(chatExist, true))
    }

    @Test
    fun postMessage() {
        println(
            StringUtil.json(
                client.postMessage(
                    chatId = client.chatIdByName("dev") ?: throw RuntimeException("channel not exist"),
                    title = "title tag1 tag2",
                    subTitle = "subtitle",
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
                    chatId = client.chatIdByName("dev") ?: throw RuntimeException("channel not exist"),
                    timeStamp = System.currentTimeMillis(),
                    title = "title",
                    message = listOf("message")
                ), true
            )
        )
    }

}