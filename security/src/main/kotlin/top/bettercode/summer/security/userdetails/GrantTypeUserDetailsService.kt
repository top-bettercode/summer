package top.bettercode.summer.security.userdetails

import jakarta.servlet.http.HttpServletRequest
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.core.userdetails.UserDetailsService

/**
 * @author Peter Wu
 */
interface GrantTypeUserDetailsService : UserDetailsService {
    fun loadUserByGrantTypeAndRequest(grantType: String, request: HttpServletRequest): UserDetails
}
