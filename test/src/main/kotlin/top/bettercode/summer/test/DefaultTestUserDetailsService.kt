package top.bettercode.summer.test

import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.core.userdetails.UserDetailsService
import top.bettercode.summer.security.authorization.UserDetailsAuthenticationToken
import top.bettercode.summer.security.userdetails.ScopeUserDetailsService

/**
 *
 * @author Peter Wu
 */
class DefaultTestUserDetailsService(private val userDetailsService: UserDetailsService) : TestUserDetailsService {

    override fun loadAuthenticationToken(scope: String, username: String) {
        val userDetails: UserDetails = if (userDetailsService is ScopeUserDetailsService) {
            userDetailsService.loadUserByScopeAndUsername(scope, username)
        } else {
            userDetailsService.loadUserByUsername(username)
        }
        SecurityContextHolder.getContext().authentication = UserDetailsAuthenticationToken(userDetails)
    }

}