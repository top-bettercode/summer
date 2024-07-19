package top.bettercode.summer.tools.lang.log.feishu

import ch.qos.logback.classic.Level
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import top.bettercode.summer.tools.lang.log.feishu.FeishuClient.Companion.template
import top.bettercode.summer.tools.lang.util.StringUtil


/**
 *
 * @author Peter Wu
 */
@Disabled
class FeishuClientTest {

    val client = FeishuClient(
        "",
        ""
    )
    private val chat = "self"

    @Test
    fun chatList() {
        val channelsList = client.chatList()
        println(StringUtil.json(channelsList, true))
    }

    @Test
    fun chatIdByName() {
        println(StringUtil.json(client.chatIdByName(chat), true))
    }

    @Test
    fun chatExist() {
        val chatExist = client.chatExist(chat)
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
                    chatId = client.chatIdByName(chat) ?: throw RuntimeException("channel not exist"),
                    title = "title tag1 tag2",
                    subTitle = "subtitle",
                    initialComment = "initialComment",
                    template = template(Level.ERROR),
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
                    chatId = client.chatIdByName(chat) ?: throw RuntimeException("channel not exist"),
                    timeStamp = System.currentTimeMillis(),
                    title = "title",
                    message = listOf("message")
                ), true
            )
        )
    }

}