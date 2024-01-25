package top.bettercode.summer.test

import org.springframework.beans.factory.annotation.Autowired
import top.bettercode.summer.web.support.ApplicationContextHolder

/**
 * mockMvc 基础测试类
 *
 * @author Peter Wu
 */
@Suppress("LeakingThis")
abstract class BaseWebAuthTest : BaseWebNoAuthTest() {

    lateinit var clientId: String
    lateinit var username: String
    lateinit var scope: String

    @Autowired
    lateinit var userDetailsService: TestAuthenticationService

    init {
        this.clientId = ""
        this.username = "root"
        this.scope = "app"
    }

    public override fun defaultBeforeEach() {
        beforeEach()
        if (clientId.isBlank()) {
            this.clientId = ApplicationContextHolder.getProperty("summer.security.client-id", "")
                    ?: ""
        }
        userDetailsService.loadAuthentication(clientId, setOf(scope), username)
    }

}
