package top.bettercode.summer.tools.weixin.test.support.offiaccount

import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import top.bettercode.summer.test.BaseTest
import top.bettercode.summer.tools.lang.util.StringUtil
import top.bettercode.summer.tools.lang.util.TimeUtil
import top.bettercode.summer.tools.weixin.support.offiaccount.OffiaccountClient
import top.bettercode.summer.tools.weixin.support.offiaccount.entity.Data
import top.bettercode.summer.tools.weixin.support.offiaccount.entity.TemplateMsgRequest
import java.time.format.DateTimeFormatter

/**
 *
 * @author Peter Wu
 */
@Disabled
internal class OffiaccountClientTest : BaseTest() {

    @Autowired
    lateinit var offiaccountClient: OffiaccountClient

    @Test
    fun getBaseAccessToken() {
        val result = offiaccountClient.getBaseAccessToken()
        System.err.println(StringUtil.valueOf(result, true))
    }

    @Test
    fun getJsapiTicket() {
        val result = offiaccountClient.getJsapiTicket()
        System.err.println(StringUtil.valueOf(result, true))
    }

    @Test
    fun getWebPageAccessToken() {
        val result =
            offiaccountClient.getWebPageAccessToken("001HKjFa19niJC0qn4Ga1dZNir0HKjFZ")
        System.err.println(StringUtil.valueOf(result, true))
    }

    @Test
    fun jsSignUrl() {
        val result = offiaccountClient.jsSignUrl("https://127.0.0.1")
        System.err.println(StringUtil.valueOf(result, true))
    }

    @Disabled
    @Test
    fun sendTemplateMsg() {
        val request = TemplateMsgRequest(
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
        val result = offiaccountClient.sendTemplateMsg(request)
        System.err.println(StringUtil.valueOf(result, true))
    }
}