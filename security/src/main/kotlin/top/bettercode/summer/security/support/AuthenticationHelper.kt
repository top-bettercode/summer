package top.bettercode.summer.security.support

import org.springframework.security.core.Authentication
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.core.userdetails.UserDetails
import java.util.*

/**
 * @author Peter Wu
 */
object AuthenticationHelper {
    private val authentication: Authentication?
        /**
         * @return 授权信息
         */
        get() = SecurityContextHolder.getContext().authentication

    @JvmStatic
    val principal: UserDetails?
        /**
         * @return 授权信息
         */
        get() {
            val authentication = authentication
            if (authentication != null) {
                val principal = authentication.principal
                if (principal is UserDetails) {
                    return principal
                }
            }
            return null
        }

    @JvmStatic
    val username: Optional<String>
        get() {
            val authentication = authentication
            if (authentication != null) {
                val principal = authentication.principal
                return if (principal is UserDetails) {
                    Optional.of(principal.username)
                } else {
                    if (principal == null) Optional.empty() else Optional.of(principal.toString())
                }
            }
            return Optional.empty()
        }

    /**
     * @param authentication 授权信息
     * @param authority      权限
     * @return 授权信息是否包含指定权限
     */
    private fun hasAuthority(authentication: Authentication, authority: String): Boolean {
        return hasAuthority(authentication.authorities, authority)
    }

    @JvmStatic
    fun hasAuthority(
            authorities: Collection<GrantedAuthority>,
            authority: String
    ): Boolean {
        for (grantedAuthority in authorities) {
            if (grantedAuthority.authority == authority) {
                return true
            }
        }
        return false
    }

    /**
     * @param authority 权限
     * @return 授权信息是否包含指定权限
     */
        @JvmStatic
    fun hasAuthority(authority: String): Boolean {
        val authentication = authentication
                ?: return false
        return hasAuthority(authentication, authority)
    }
}
