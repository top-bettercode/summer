package top.bettercode.summer.security.userdetails

import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.util.Assert
import org.springframework.web.context.request.RequestContextHolder
import org.springframework.web.context.request.ServletRequestAttributes
import top.bettercode.summer.security.support.AuthenticationHelper
import top.bettercode.summer.security.support.SecurityParameterNames

/**
 * @author Peter Wu
 */
interface ClientScopeUserDetailsService : UserDetailsService {

    fun loadUserByScopeAndUsername(clientd: String, scope: String, username: String): UserDetails

    fun loadUserByScopeAndUsername(clientd: String, scope: Set<String>, username: String): UserDetails {
        return loadUserByScopeAndUsername(clientd, scope.first(), username)
    }

    override fun loadUserByUsername(username: String): UserDetails {
        val requestAttributes = RequestContextHolder.getRequestAttributes() as ServletRequestAttributes
        val request = requestAttributes.request
        val scope = request.getParameterValues(SecurityParameterNames.SCOPE)?.toSet() ?: emptySet()
        Assert.isTrue(scope.isNotEmpty(), "scope 不能为空")
        val clientId = AuthenticationHelper.getClientInfo(request)?.first
                ?: throw IllegalArgumentException("客户端未授权")
        return loadUserByScopeAndUsername(clientId, scope, username)
    }
}
