package top.bettercode.summer.test

import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.core.userdetails.UserDetailsService
import top.bettercode.summer.security.authorization.UserDetailsAuthenticationToken
import top.bettercode.summer.security.userdetails.ClientUserDetailsService

/**
 *
 * @author Peter Wu
 */
class DefaultTestAuthenticationService(private val userDetailsService: UserDetailsService) : TestAuthenticationService {

    override fun loadAuthentication(clientd: String, scope: Set<String>, username: String) {
        val userDetails: UserDetails = if (userDetailsService is ClientUserDetailsService) {
            userDetailsService.loadUserByClientAndUsername(clientd, scope, username)
        } else {
            userDetailsService.loadUserByUsername(username)
        }
        SecurityContextHolder.getContext().authentication = UserDetailsAuthenticationToken(userDetails)
    }

}