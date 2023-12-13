package top.bettercode.summer.test

import org.springframework.beans.factory.annotation.Autowired
import top.bettercode.summer.web.support.ApplicationContextHolder

/**
 * mockMvc 基础测试类
 *
 * @author Peter Wu
 */
@Suppress("LeakingThis", "JoinDeclarationAndAssignment")
abstract class BaseWebAuthTest : BaseWebNoAuthTest() {

    lateinit var clientId: String
    lateinit var username: String
    lateinit var scope: Set<String>

    @Autowired
    lateinit var userDetailsService: TestAuthenticationService

    init {
        this.clientId = ApplicationContextHolder.getProperty("summer.security.client-id", "") ?: ""
        this.username = "root"
        this.scope = setOf("app")
    }

    protected fun setScope(scope: String) {
        this.scope = setOf(scope)
    }

    public override fun defaultBeforeEach() {
        beforeEach()
        userDetailsService.loadAuthentication(clientId, scope, username)
    }

}
