package top.bettercode.summer.security.userdetails

import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.core.userdetails.UserDetailsService
import javax.servlet.http.HttpServletRequest

/**
 * @author Peter Wu
 */
interface GrantTypeUserDetailsService : UserDetailsService {
    fun loadUserByGrantTypeAndRequest(grantType: String?, request: HttpServletRequest?): UserDetails
}
