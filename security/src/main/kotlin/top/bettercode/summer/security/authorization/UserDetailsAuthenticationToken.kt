package top.bettercode.summer.security.authorization

import org.springframework.security.authentication.AbstractAuthenticationToken
import org.springframework.security.core.userdetails.UserDetails
import top.bettercode.summer.security.token.TokenId

/**
 * @author Peter Wu
 */
class UserDetailsAuthenticationToken(
    var id: TokenId,
    private val userDetails: UserDetails
) : AbstractAuthenticationToken(userDetails.authorities) {
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
