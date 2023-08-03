package top.bettercode.summer.security.authorization

import org.springframework.security.authentication.AbstractAuthenticationToken
import org.springframework.security.core.userdetails.UserDetails

/**
 * @author Peter Wu
 */
class UserDetailsAuthenticationToken(private val userDetails: UserDetails) : AbstractAuthenticationToken(userDetails.authorities) {
    init {
        isAuthenticated = true
    }

    override fun getCredentials(): Any {
        return "N/A"
    }

    override fun getPrincipal(): Any {
        return userDetails
    }

}
