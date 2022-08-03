package top.bettercode.summer.util.mobile

import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import top.bettercode.summer.util.test.BaseTest

/**
 *
 * @author Peter Wu
 */
class MobileQueryClientTest : BaseTest() {

    @Autowired
    lateinit var mobileQueryClient: MobileQueryClient

    @Test
    fun query() {
        mobileQueryClient.query("")
    }
}