package top.bettercode.summer.logging.feishu

import org.junit.jupiter.api.Test
import top.bettercode.summer.tools.lang.util.StringUtil

/**
 *
 * @author Peter Wu
 */
class FeishuClientTest {

    val client = FeishuClient(
        "",
        "",
        "./build",
        "/actuator"
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
                    client.chatIdByName("dev") ?: throw RuntimeException("channel not exist"),
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
                client.filesUpload(
                    client.chatIdByName("dev") ?: throw RuntimeException("channel not exist"),
                    System.currentTimeMillis(),
                    "title",
                    listOf("message")
                ), true
            )
        )
    }

}