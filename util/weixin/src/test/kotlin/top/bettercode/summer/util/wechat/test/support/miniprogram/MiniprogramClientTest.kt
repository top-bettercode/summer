package top.bettercode.summer.util.wechat.test.support.miniprogram

import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import top.bettercode.lang.util.StringUtil
import top.bettercode.summer.util.test.BaseTest
import top.bettercode.summer.util.wechat.support.miniprogram.MiniprogramClient

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
}