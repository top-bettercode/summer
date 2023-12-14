package top.bettercode.summer.security.support

import org.springframework.http.HttpHeaders
import org.springframework.security.authentication.BadCredentialsException
import org.springframework.security.core.Authentication
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.core.userdetails.UserDetails
import java.nio.charset.StandardCharsets
import java.util.*
import javax.servlet.http.HttpServletRequest

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

    fun getClientInfo(request: HttpServletRequest): Pair<String?, String?> {
        var header = request.getHeader(HttpHeaders.AUTHORIZATION)
        if (header != null) {
            header = header.trim()
            if (header.startsWith("Basic", true) && !header.equals("Basic", ignoreCase = true)) {
                val basicCredentials = String(
                        decode(header.substring(6).toByteArray(StandardCharsets.UTF_8)), StandardCharsets.UTF_8)
                val clientInfo = basicCredentials.split(":")
                return Pair(clientInfo[0], clientInfo[1])
            }
        }
        val clientId = request.getParameter("client_id")
        val clientSecret = request.getParameter("client_secret")
        return Pair(clientId, clientSecret)
    }

    private fun decode(base64Token: ByteArray): ByteArray {
        return try {
            Base64.getDecoder().decode(base64Token)
        } catch (var3: IllegalArgumentException) {
            throw BadCredentialsException("Failed to decode basic authentication token")
        }
    }

}
