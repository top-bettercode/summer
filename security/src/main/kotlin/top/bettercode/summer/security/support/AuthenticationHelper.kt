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
    private val authentication: Optional<Authentication>
        /**
         * @return 授权信息
         */
        get() = Optional.ofNullable(SecurityContextHolder.getContext().authentication)

    @JvmStatic
    val principal: Optional<UserDetails>
        /**
         * @return 授权信息
         */
        get() {
            return authentication.map {
                val principal = it.principal
                if (principal is UserDetails) {
                    principal
                } else {
                    null
                }
            }
        }

    @JvmStatic
    val username: Optional<String>
        get() {
            return authentication.map {
                val principal = it.principal
                if (principal is UserDetails) {
                    principal.username
                } else {
                    principal?.toString()
                }
            }
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
        return authentication.map {
            hasAuthority(it.authorities, authority)
        }.orElse(false)
    }

    @JvmStatic
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

    @JvmStatic
    fun getClientId(request: HttpServletRequest): String {
        return getClientInfo(request).first
                ?: throw IllegalArgumentException("客户端未授权")
    }

    private fun decode(base64Token: ByteArray): ByteArray {
        return try {
            Base64.getDecoder().decode(base64Token)
        } catch (var3: IllegalArgumentException) {
            throw BadCredentialsException("Failed to decode basic authentication token")
        }
    }

}
