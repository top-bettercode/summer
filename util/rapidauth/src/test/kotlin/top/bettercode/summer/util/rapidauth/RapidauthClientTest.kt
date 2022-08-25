package top.bettercode.summer.util.rapidauth

import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import top.bettercode.summer.util.test.BaseTest

/**
 *
 * @author Peter Wu
 */
class RapidauthClientTest : BaseTest() {

    @Autowired
    lateinit var rapidauthClient: IRapidauthClient

    @Test
    fun query() {
        rapidauthClient.query("mobile", "xx")
    }
}