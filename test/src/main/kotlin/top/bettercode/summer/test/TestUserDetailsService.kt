package top.bettercode.summer.test

import org.springframework.security.core.userdetails.UserDetails

/**
 *
 * @author Peter Wu
 */
interface TestUserDetailsService {

    fun loadUserByScopeAndUsername(scope: String, username: String): UserDetails

}