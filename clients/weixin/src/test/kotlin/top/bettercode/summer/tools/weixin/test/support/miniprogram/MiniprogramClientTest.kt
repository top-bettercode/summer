package top.bettercode.summer.tools.weixin.test.support.miniprogram

import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import top.bettercode.summer.test.BaseTest
import top.bettercode.summer.tools.lang.util.StringUtil
import top.bettercode.summer.tools.lang.util.TimeUtil
import top.bettercode.summer.tools.weixin.support.miniprogram.MiniprogramClient
import top.bettercode.summer.tools.weixin.support.miniprogram.entity.MiniData
import top.bettercode.summer.tools.weixin.support.miniprogram.entity.SubscribeMsgRequest
import java.time.format.DateTimeFormatter

/**
 *
 * @author Peter Wu
 */
@Disabled
internal class MiniprogramClientTest : BaseTest() {

    @Autowired
    lateinit var miniprogramClient: MiniprogramClient

    @Test
    fun jscode2session() {
        val result = miniprogramClient.jscode2session("xxx")
        System.err.println(StringUtil.json(result, true))
    }

    @Disabled
    @Test
    fun getuserphonenumber() {
        val result = miniprogramClient.getuserphonenumber("xxx")
        System.err.println(StringUtil.json(result, true))
    }

    @Disabled
    @Test
    fun sendSubscribeMsg() {
        val request = SubscribeMsgRequest(
                "",
                "",
                MiniData().of(
                        "first", "抱歉，您参与的竞拍未中标！")
                        .of("keyword1", "xxxxx")
                        .of("keyword2", TimeUtil.now()
                                .format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))
                                + "至" + TimeUtil.now()
                                .format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))
                        )
                        .of("keyword3", "25.5")

        )
        val result = miniprogramClient.sendSubscribeMsg(request)
        System.err.println(StringUtil.json(result, true))
    }
}