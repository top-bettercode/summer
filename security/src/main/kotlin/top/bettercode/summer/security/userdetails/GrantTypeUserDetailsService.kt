package top.bettercode.summer.security.userdetails

import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.security.core.userdetails.UsernameNotFoundException
import javax.servlet.http.HttpServletRequest

/**
 * @author Peter Wu
 */
interface GrantTypeUserDetailsService : UserDetailsService {
    @Throws(UsernameNotFoundException::class)
    fun loadUserByGrantTypeAndRequest(grantType: String?, request: HttpServletRequest?): UserDetails
}
