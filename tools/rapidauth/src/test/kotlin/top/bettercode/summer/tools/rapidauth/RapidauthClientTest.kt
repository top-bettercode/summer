package top.bettercode.summer.tools.rapidauth

import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import top.bettercode.summer.test.BaseTest

/**
 *
 * @author Peter Wu
 */
class RapidauthClientTest : BaseTest() {

    @Autowired
    lateinit var rapidauthClient: IRapidauthClient

    @Disabled
    @Test
    fun query() {
        rapidauthClient.query("mobile", "xx")
    }
}