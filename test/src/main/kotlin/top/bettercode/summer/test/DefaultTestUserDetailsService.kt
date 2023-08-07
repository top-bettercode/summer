package top.bettercode.summer.test

import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.core.userdetails.UserDetailsService
import top.bettercode.summer.security.userdetails.ScopeUserDetailsService

/**
 *
 * @author Peter Wu
 */
class DefaultTestUserDetailsService(private val userDetailsService: UserDetailsService) : TestUserDetailsService {

    override fun loadUserByScopeAndUsername(scope: String, username: String): UserDetails {
        val userDetails: UserDetails = if (userDetailsService is ScopeUserDetailsService) {
            userDetailsService.loadUserByScopeAndUsername(scope, username)
        } else {
            userDetailsService.loadUserByUsername(username)
        }
        return userDetails
    }

}