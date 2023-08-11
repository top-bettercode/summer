package top.bettercode.summer.security.userdetails

import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.util.Assert
import org.springframework.web.context.request.RequestContextHolder
import org.springframework.web.context.request.ServletRequestAttributes
import top.bettercode.summer.security.support.SecurityParameterNames

/**
 * @author Peter Wu
 */
interface ScopeUserDetailsService : UserDetailsService {
    fun loadUserByScopeAndUsername(scope: String, username: String): UserDetails

    override fun loadUserByUsername(username: String): UserDetails {
        val requestAttributes = RequestContextHolder.getRequestAttributes() as ServletRequestAttributes
        val request = requestAttributes.request
        val scope = request.getParameter(SecurityParameterNames.SCOPE)
        Assert.hasText(scope, "scope 不能为空")
        return loadUserByScopeAndUsername(scope, username)
    }
}
