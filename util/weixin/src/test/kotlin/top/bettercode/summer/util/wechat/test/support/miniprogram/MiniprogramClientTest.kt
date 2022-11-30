package top.bettercode.summer.util.wechat.test.support.miniprogram

import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import top.bettercode.lang.util.TimeUtil
import top.bettercode.lang.util.StringUtil
import top.bettercode.summer.util.test.BaseTest
import top.bettercode.summer.util.wechat.support.miniprogram.IMiniprogramClient
import top.bettercode.summer.util.wechat.support.miniprogram.entity.Data
import top.bettercode.summer.util.wechat.support.miniprogram.entity.SubscribeMsgRequest
import java.time.format.DateTimeFormatter

/**
 *
 * @author Peter Wu
 */
internal class MiniprogramClientTest : BaseTest() {

    @Autowired
    lateinit var miniprogramClient: IMiniprogramClient

    @Test
    fun jscode2session() {
        val result = miniprogramClient.jscode2session("xxx")
        System.err.println(StringUtil.valueOf(result, true))
    }

    @Disabled
    @Test
    fun getuserphonenumber() {
        val result = miniprogramClient.getuserphonenumber("xxx")
        System.err.println(StringUtil.valueOf(result, true))
    }

    @Disabled
    @Test
    fun sendSubscribeMsg() {
        val request = SubscribeMsgRequest(
            "",
            "",
            mapOf(
                "first" to Data("抱歉，您参与的竞拍未中标！"),
                "keyword1" to Data("xxxxx"),
                "keyword2" to Data(
                    TimeUtil.now()
                        .format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))
                            + "至" + TimeUtil.now()
                        .format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))
                ),
                "keyword3" to Data("25.5")
            )
        )
        val result = miniprogramClient.sendSubscribeMsg(request)
        System.err.println(StringUtil.valueOf(result, true))
    }
}