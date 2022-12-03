package top.bettercode.summer.tools.mobile

import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import top.bettercode.summer.test.BaseTest

/**
 *
 * @author Peter Wu
 */
class MobileQueryClientTest : BaseTest() {

    @Autowired
    lateinit var mobileQueryClient: IMobileQueryClient

    @Disabled
    @Test
    fun query() {
        mobileQueryClient.query("xxx")
    }
}