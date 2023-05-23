package top.bettercode.summer.security.authorization

import org.springframework.security.authentication.AuthenticationProvider
import org.springframework.security.core.Authentication

class UserDetailsAuthenticationProvider : AuthenticationProvider {
    override fun authenticate(authentication: Authentication): Authentication {
        authentication.isAuthenticated = true
        return authentication
    }

    override fun supports(authentication: Class<*>): Boolean {
        return UserDetailsAuthenticationToken::class.java.isAssignableFrom(authentication)
    }
}
