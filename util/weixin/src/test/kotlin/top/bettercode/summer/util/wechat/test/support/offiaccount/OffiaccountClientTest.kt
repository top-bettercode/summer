package top.bettercode.summer.util.wechat.test.support.offiaccount

import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import top.bettercode.lang.util.StringUtil
import top.bettercode.summer.util.test.BaseTest
import top.bettercode.summer.util.wechat.support.offiaccount.OffiaccountClient

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
}