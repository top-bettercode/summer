package top.bettercode.summer.tools.lang.log.feishu

import ch.qos.logback.classic.Level
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import top.bettercode.summer.tools.lang.log.feishu.FeishuMsgClient.Companion.template
import top.bettercode.summer.tools.lang.util.StringUtil

/**
 *
 * @author Peter Wu
 */
@Disabled
class FeishuMsgHookClientTest {
    val client = FeishuMsgHookClient(
        "",
        ""
    )

    @Test
    fun postMessage() {
        println(
            StringUtil.json(
                client.postMessage(
                    title = "title tag1 tag2",
                    subTitle = "subtitle",
                    initialComment = "initialComment",
                    template = template(Level.ERROR),
                    message = null,
                    logUrl = "http://localhost:8080/actuator/logs",
                    linkTitle = "logging"
                ), true
            )
        )
    }
}