package top.bettercode.summer.util.wechat.test.support.miniprogram

import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import top.bettercode.lang.util.LocalDateTimeHelper
import top.bettercode.lang.util.StringUtil
import top.bettercode.summer.util.test.BaseTest
import top.bettercode.summer.util.wechat.support.miniprogram.MiniprogramClient
import top.bettercode.summer.util.wechat.support.miniprogram.entity.Data
import top.bettercode.summer.util.wechat.support.miniprogram.entity.SubscribeMsgRequest
import java.time.format.DateTimeFormatter

/**
 *
 * @author Peter Wu
 */
internal class MiniprogramClientTest : BaseTest() {

    @Autowired
    lateinit var miniprogramClient: MiniprogramClient

    @Test
    fun code2Session() {
        val result = miniprogramClient.code2Session("xxx")
        System.err.println(StringUtil.valueOf(result, true))
    }

    @Test
    fun sendSubscribeMsg() {
        val request = SubscribeMsgRequest(
            "",
            "",
            mapOf(
                "first" to Data("抱歉，您参与的竞拍未中标！"),
                "keyword1" to Data("xxxxx"),
                "keyword2" to Data(
                    LocalDateTimeHelper.now()
                        .format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))
                            + "至" + LocalDateTimeHelper.now()
                        .format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))
                ),
                "keyword3" to Data("25.5")
            )
        )
        val result = miniprogramClient.sendSubscribeMsg(request)
        System.err.println(StringUtil.valueOf(result, true))
    }
}