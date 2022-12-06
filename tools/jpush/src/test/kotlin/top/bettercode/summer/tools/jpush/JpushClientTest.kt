package top.bettercode.summer.tools.jpush

import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import top.bettercode.summer.test.BaseTest
import top.bettercode.summer.tools.jpush.entity.Audience
import top.bettercode.summer.tools.jpush.entity.JpushRequest
import top.bettercode.summer.tools.jpush.entity.Message
import top.bettercode.summer.tools.jpush.entity.Notification

/**
 *
 * @author Peter Wu
 */
class JpushClientTest : BaseTest() {

    @Autowired
    lateinit var jpushClient: JpushClient

    @Disabled
    @Test
    fun send() {
        val extras = mapOf("key" to "value")
        val request = JpushRequest(
            Audience("123"),
            Notification("msg", extras),
            Message("msg", extras)
        )
        jpushClient.send(request)
    }
}