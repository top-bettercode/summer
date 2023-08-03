package top.bettercode.summer.test

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.core.userdetails.UserDetailsService
import top.bettercode.summer.security.authorization.UserDetailsAuthenticationToken
import top.bettercode.summer.security.userdetails.ScopeUserDetailsService

/**
 * mockMvc 基础测试类
 *
 * @author Peter Wu
 */
abstract class BaseWebAuthTest : BaseWebNoAuthTest() {

    lateinit var username: String
    lateinit var scope: String

    @Autowired
    lateinit var userDetailsService: UserDetailsService

    init {
        this.username = "root"
        this.scope = "app"
    }

    public override fun defaultBeforeEach() {
        beforeEach()
        val userDetails: UserDetails = if (userDetailsService is ScopeUserDetailsService) {
            (userDetailsService as ScopeUserDetailsService).loadUserByScopeAndUsername(scope, username)
        } else {
            userDetailsService.loadUserByUsername(username)
        }
        SecurityContextHolder.getContext().authentication = UserDetailsAuthenticationToken(userDetails)
    }
}
