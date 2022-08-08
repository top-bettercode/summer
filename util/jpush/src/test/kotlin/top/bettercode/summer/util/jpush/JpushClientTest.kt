package top.bettercode.summer.util.jpush

import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import top.bettercode.summer.util.jpush.entity.*
import top.bettercode.summer.util.test.BaseTest

/**
 *
 * @author Peter Wu
 */
class JpushClientTest : BaseTest() {

    @Autowired
    lateinit var jpushClient: JpushClient

    @Test
    fun send() {
        val extras = mapOf("key" to "value")
        val request = JpushRequest(
            Audience("123"),
            Notification("msg", extras),
            Message("1", "msg", extras)
        )
        jpushClient.send(request)
    }
}