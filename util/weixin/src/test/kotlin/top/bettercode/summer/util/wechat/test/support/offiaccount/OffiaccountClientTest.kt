package top.bettercode.summer.util.wechat.test.support.offiaccount

import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import top.bettercode.lang.util.LocalDateTimeHelper
import top.bettercode.lang.util.StringUtil
import top.bettercode.summer.util.test.BaseTest
import top.bettercode.summer.util.wechat.support.offiaccount.OffiaccountClient
import top.bettercode.summer.util.wechat.support.offiaccount.entity.TemplateMsgParam
import top.bettercode.summer.util.wechat.support.offiaccount.entity.TemplateMsgRequest
import java.time.format.DateTimeFormatter

/**
 *
 * @author Peter Wu
 */
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

    @Test
    internal fun sendTemplateMessage() {
        val request = TemplateMsgRequest(
            "",
            "",
            mapOf(
                "first" to TemplateMsgParam("抱歉，您参与的竞拍未中标！"),
                "keyword1" to TemplateMsgParam("xxxxx"),
                "keyword2" to TemplateMsgParam(
                    LocalDateTimeHelper.now()
                        .format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))
                            + "至" + LocalDateTimeHelper.now()
                        .format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))
                ),
                "keyword3" to TemplateMsgParam("25.5")
            )
        )
        val result = offiaccountClient.sendTemplateMsg(request)
        System.err.println(StringUtil.valueOf(result, true))
    }
}