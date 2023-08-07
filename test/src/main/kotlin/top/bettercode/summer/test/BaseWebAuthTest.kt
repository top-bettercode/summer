package top.bettercode.summer.test

import org.springframework.beans.factory.annotation.Autowired

/**
 * mockMvc 基础测试类
 *
 * @author Peter Wu
 */
abstract class BaseWebAuthTest : BaseWebNoAuthTest() {

    lateinit var username: String
    lateinit var scope: String

    @Autowired
    lateinit var userDetailsService: TestAuthenticationService

    init {
        this.username = "root"
        this.scope = "app"
    }

    public override fun defaultBeforeEach() {
        beforeEach()
        userDetailsService.loadAuthentication(scope, username)
    }

}
