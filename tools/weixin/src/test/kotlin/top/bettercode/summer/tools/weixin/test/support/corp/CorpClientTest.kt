package top.bettercode.summer.tools.weixin.test.support.corp

import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import top.bettercode.summer.test.BaseTest
import top.bettercode.summer.tools.lang.util.StringUtil
import top.bettercode.summer.tools.weixin.support.corp.CorpClient

/**
 *
 * @author Peter Wu
 */
@Disabled
internal class CorpClientTest: BaseTest() {


    @Autowired
    lateinit var corpClient: CorpClient

    @Test
    fun getBaseAccessToken() {
        val result = corpClient.getBaseAccessToken()
        System.err.println(StringUtil.valueOf(result, true))
    }

    @Test
    fun getWebPageAccessToken() {
        val result =
            corpClient.getWebPageAccessToken("001HKjFa19niJC0qn4Ga1dZNir0HKjFZ")
        System.err.println(StringUtil.valueOf(result, true))
    }


}